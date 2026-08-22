package edu.demart_api.service.impl;

import edu.demart_api.dto.request.ProcessReturnRequest;
import edu.demart_api.dto.request.ReturnExchangeRequest;
import edu.demart_api.dto.response.ReturnExchangeResponse;
import edu.demart_api.entity.*;
import edu.demart_api.exception.BusinessException;
import edu.demart_api.exception.ResourceNotFoundException;
import edu.demart_api.repository.OrderRepository;
import edu.demart_api.repository.ProductRepository;
import edu.demart_api.repository.ReturnExchangeRepository;
import edu.demart_api.service.ReturnExchangeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReturnExchangeServiceImpl implements ReturnExchangeService {

    private static final int RETURN_WINDOW_DAYS = 7;

    private final ReturnExchangeRepository returnExchangeRepository;
    private final OrderRepository          orderRepository;
    private final ProductRepository        productRepository;

    public ReturnExchangeServiceImpl(ReturnExchangeRepository returnExchangeRepository,
                                     OrderRepository orderRepository,
                                     ProductRepository productRepository) {
        this.returnExchangeRepository = returnExchangeRepository;
        this.orderRepository          = orderRepository;
        this.productRepository        = productRepository;
    }

    // ─── Customer: Submit Request ─────────────────────────────────────────────

    @Override
    @Transactional
    public ReturnExchangeResponse requestReturn(Long userId, Long orderId, ReturnExchangeRequest request) {

        // 1. Load order and verify ownership
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        // 2. Order must be DELIVERED to accept returns
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(
                    "Returns/exchanges are only accepted for DELIVERED orders. Current status: " + order.getStatus());
        }

        // 3. Check delivery timestamp exists
        if (order.getDeliveredAt() == null) {
            throw new BusinessException("Cannot process return — delivery timestamp is missing");
        }

        // 4. Check eligibility window (7 days from delivery)
        LocalDateTime eligibleUntil = order.getDeliveredAt().plusDays(RETURN_WINDOW_DAYS);
        if (LocalDateTime.now().isAfter(eligibleUntil)) {
            throw new BusinessException(
                    "Return/exchange window has expired. Requests must be submitted within "
                    + RETURN_WINDOW_DAYS + " days of delivery (deadline: " + eligibleUntil + ")");
        }

        // 5. Locate the order item
        OrderItem orderItem = order.getItems().stream()
                .filter(i -> i.getId().equals(request.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order item not found: " + request.getOrderItemId()));

        // 6. Item must not already be returned/exchanged
        if (orderItem.isReturned()) {
            throw new BusinessException("This item has already been returned or exchanged");
        }

        // 7. No active (PENDING/APPROVED) request already exists for this item
        if (returnExchangeRepository.existsByOrderItemIdAndStatusIn(
                orderItem.getId(), List.of(ReturnExchangeStatus.PENDING, ReturnExchangeStatus.APPROVED))) {
            throw new BusinessException(
                    "A return/exchange request for this item is already in progress");
        }

        // 8. Parse and validate type
        ReturnExchangeType type = parseType(request.getType());

        // 9. Resolve target product for EXCHANGE
        Product targetProduct = null;
        if (type == ReturnExchangeType.EXCHANGE && request.getTargetProductId() != null) {
            targetProduct = productRepository.findByIdAndActiveTrue(request.getTargetProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Target product not found: " + request.getTargetProductId()));
        }

        // 10. Build and save the request
        ReturnExchange re = new ReturnExchange();
        re.setOrder(order);
        re.setOrderItem(orderItem);
        re.setType(type);
        re.setReason(request.getReason());
        re.setTargetProduct(targetProduct);
        re.setEligibleUntil(eligibleUntil);
        re.setStatus(ReturnExchangeStatus.PENDING);

        return toResponse(returnExchangeRepository.save(re));
    }

    // ─── Customer: View Own Requests ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ReturnExchangeResponse> getMyRequests(Long userId) {
        return returnExchangeRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    // ─── Staff: View All Requests ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ReturnExchangeResponse> getAllRequests(String statusStr) {
        ReturnExchangeStatus status = statusStr != null ? parseStatus(statusStr) : null;
        return returnExchangeRepository.findAllWithFilters(status)
                .stream().map(this::toResponse).toList();
    }

    // ─── Staff: Approve or Reject ─────────────────────────────────────────────

    @Override
    @Transactional
    public ReturnExchangeResponse processRequest(Long requestId, ProcessReturnRequest request) {
        ReturnExchange re = returnExchangeRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Return/exchange request not found: " + requestId));

        if (re.getStatus() != ReturnExchangeStatus.PENDING) {
            throw new BusinessException(
                    "Request has already been processed. Current status: " + re.getStatus());
        }

        String action = request.getAction().toUpperCase();

        if ("APPROVE".equals(action)) {
            handleApproval(re);
            re.setStatus(ReturnExchangeStatus.APPROVED);
        } else if ("REJECT".equals(action)) {
            if (request.getStaffNote() == null || request.getStaffNote().isBlank()) {
                throw new BusinessException("Staff note is required when rejecting a request");
            }
            re.setStatus(ReturnExchangeStatus.REJECTED);
        } else {
            throw new BusinessException("Invalid action: '" + action + "'. Use APPROVE or REJECT");
        }

        re.setStaffNote(request.getStaffNote());
        re.setProcessedAt(LocalDateTime.now());

        return toResponse(returnExchangeRepository.save(re));
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private void handleApproval(ReturnExchange re) {
        OrderItem item = re.getOrderItem();
        Product original = item.getProduct();

        if (re.getType() == ReturnExchangeType.RETURN) {
            // Restock the returned product
            productRepository.incrementStock(original.getId(), item.getQuantity());

        } else { // EXCHANGE
            // Determine the target product (same product if none specified)
            Product target = re.getTargetProduct() != null ? re.getTargetProduct() : original;

            // Check target product has sufficient stock for exchange
            if (re.getTargetProduct() != null) {
                int updated = productRepository.decrementStock(target.getId(), item.getQuantity());
                if (updated == 0) {
                    throw new BusinessException(
                            "Cannot approve exchange — insufficient stock for target product '"
                            + target.getName() + "'");
                }
                // Restock original
                productRepository.incrementStock(original.getId(), item.getQuantity());
            } else {
                // Same product replacement — just restock (a new physical unit is being given)
                // No stock change needed since we're physically replacing from existing stock
                int updated = productRepository.decrementStock(original.getId(), item.getQuantity());
                if (updated == 0) {
                    throw new BusinessException(
                            "Cannot approve exchange — insufficient replacement stock for '"
                            + original.getName() + "'");
                }
                productRepository.incrementStock(original.getId(), item.getQuantity());
            }
        }

        // Mark item as returned to prevent future duplicate requests
        item.setReturned(true);
    }

    private ReturnExchangeType parseType(String value) {
        try {
            return ReturnExchangeType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid type: '" + value + "'. Use RETURN or EXCHANGE");
        }
    }

    private ReturnExchangeStatus parseStatus(String value) {
        try {
            return ReturnExchangeStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: '" + value + "'. Use PENDING, APPROVED, or REJECTED");
        }
    }

    private ReturnExchangeResponse toResponse(ReturnExchange re) {
        OrderItem item = re.getOrderItem();
        Product target = re.getTargetProduct();

        return ReturnExchangeResponse.builder()
                .id(re.getId())
                .orderId(re.getOrder() != null ? re.getOrder().getId() : null)
                .orderItemId(item != null ? item.getId() : null)
                .productName(item != null ? item.getProductName() : null)
                .quantity(item != null ? item.getQuantity() : 0)
                .type(re.getType() != null ? re.getType().name() : null)
                .status(re.getStatus() != null ? re.getStatus().name() : null)
                .reason(re.getReason())
                .targetProductId(target != null ? target.getId() : null)
                .targetProductName(target != null ? target.getName() : null)
                .eligibleUntil(re.getEligibleUntil())
                .withinEligibilityWindow(re.getEligibleUntil() != null
                        && LocalDateTime.now().isBefore(re.getEligibleUntil()))
                .staffNote(re.getStaffNote())
                .processedAt(re.getProcessedAt())
                .requestedAt(re.getCreatedAt())
                .updatedAt(re.getUpdatedAt())
                .build();
    }
}
