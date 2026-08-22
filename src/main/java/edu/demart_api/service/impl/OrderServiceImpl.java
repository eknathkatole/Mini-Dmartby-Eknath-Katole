package edu.demart_api.service.impl;

import edu.demart_api.dto.request.OrderItemRequest;
import edu.demart_api.dto.request.OrderStatusUpdateRequest;
import edu.demart_api.dto.request.PlaceOrderRequest;
import edu.demart_api.dto.response.OrderItemResponse;
import edu.demart_api.dto.response.OrderResponse;
import edu.demart_api.dto.response.PageResponse;
import edu.demart_api.entity.*;
import edu.demart_api.exception.BusinessException;
import edu.demart_api.exception.ResourceNotFoundException;
import edu.demart_api.repository.OrderRepository;
import edu.demart_api.repository.ProductRepository;
import edu.demart_api.repository.UserRepository;
import edu.demart_api.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DELIVERY_CHARGE          = new BigDecimal("50.00");

    private final OrderRepository    orderRepository;
    private final ProductRepository  productRepository;
    private final UserRepository     userRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository) {
        this.orderRepository   = orderRepository;
        this.productRepository = productRepository;
        this.userRepository    = userRepository;
    }

    // ─── Customer: Place Order ────────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FulfillmentType fulfillmentType = parseFulfillmentType(request.getFulfillmentType());
        validateFulfillmentDetails(request, fulfillmentType);

        Order order = new Order();
        order.setUser(user);
        order.setFulfillmentType(fulfillmentType);
        order.setNotes(request.getNotes());
        order.setStatus(OrderStatus.PLACED);

        if (fulfillmentType == FulfillmentType.HOME_DELIVERY) {
            order.setDeliveryAddress(mapDeliveryAddress(request));
        } else {
            order.setPickupSlot(request.getPickupSlot());
        }

        // ─── Build items & deduct stock ────────────────────────────────────────
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndActiveTrue(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found or unavailable: id=" + itemReq.getProductId()));

            // Atomic stock decrement — returns 0 rows if insufficient stock
            int updated = productRepository.decrementStock(product.getId(), itemReq.getQuantity());
            if (updated == 0) {
                throw new BusinessException(
                        "Insufficient stock for '" + product.getName() +
                        "'. Requested: " + itemReq.getQuantity() +
                        ", Available: " + product.getStockQuantity());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setProductUnit(product.getUnit().name());
            item.setUnitPrice(product.getSellingPrice());
            item.setQuantity(itemReq.getQuantity());
            item.setTotalPrice(product.getSellingPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            order.getItems().add(item);

            subtotal = subtotal.add(item.getTotalPrice());
        }

        // ─── Calculate delivery charge ─────────────────────────────────────────
        BigDecimal charge = (fulfillmentType == FulfillmentType.HOME_DELIVERY
                && subtotal.compareTo(FREE_DELIVERY_THRESHOLD) < 0)
                ? DELIVERY_CHARGE : BigDecimal.ZERO;

        order.setSubtotal(subtotal);
        order.setDeliveryCharge(charge);
        order.setTotalAmount(subtotal.add(charge));

        return toResponse(orderRepository.save(order));
    }

    // ─── Customer: View Orders ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toResponse(order);
    }

    // ─── Customer: Cancel Order ───────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse cancelMyOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Order cannot be cancelled in status: " + order.getStatus()
                    + ". Only PLACED or CONFIRMED orders can be cancelled.");
        }

        restoreStockForOrder(order);
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    // ─── Staff: View All Orders ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(String statusStr, String fulfillmentTypeStr, Pageable pageable) {
        OrderStatus status = statusStr != null ? parseOrderStatus(statusStr) : null;
        FulfillmentType type = fulfillmentTypeStr != null ? parseFulfillmentType(fulfillmentTypeStr) : null;

        return PageResponse.of(
                orderRepository.findAllWithFilters(status, type, pageable)
                        .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toResponse(order);
    }

    // ─── Staff: Update Order Status ───────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        OrderStatus newStatus = parseOrderStatus(request.getStatus());
        validateStatusTransition(order, newStatus);

        // Restore stock when staff cancels an order
        if (newStatus == OrderStatus.CANCELLED) {
            restoreStockForOrder(order);
        }

        // Record delivery timestamp (enables return eligibility window)
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private void validateStatusTransition(Order order, OrderStatus newStatus) {
        OrderStatus current = order.getStatus();
        FulfillmentType type = order.getFulfillmentType();

        boolean valid = switch (current) {
            case PLACED    -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.PREPARING  || newStatus == OrderStatus.CANCELLED;
            case PREPARING -> (newStatus == OrderStatus.READY_FOR_PICKUP && type == FulfillmentType.STORE_PICKUP)
                           || (newStatus == OrderStatus.OUT_FOR_DELIVERY  && type == FulfillmentType.HOME_DELIVERY)
                           || newStatus == OrderStatus.CANCELLED;
            case READY_FOR_PICKUP -> newStatus == OrderStatus.DELIVERED;
            case OUT_FOR_DELIVERY -> newStatus == OrderStatus.DELIVERED;
            default -> false; // DELIVERED and CANCELLED are terminal
        };

        if (!valid) {
            throw new BusinessException(
                    "Invalid status transition: " + current + " → " + newStatus
                    + (type != null ? " (fulfillment: " + type + ")" : ""));
        }
    }

    private void restoreStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            if (!item.isReturned()) {
                productRepository.incrementStock(item.getProduct().getId(), item.getQuantity());
            }
        }
    }

    private FulfillmentType parseFulfillmentType(String value) {
        try {
            return FulfillmentType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid fulfillment type: '" + value + "'. Use STORE_PICKUP or HOME_DELIVERY");
        }
    }

    private OrderStatus parseOrderStatus(String value) {
        try {
            return OrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid order status: '" + value + "'");
        }
    }

    private void validateFulfillmentDetails(PlaceOrderRequest request, FulfillmentType type) {
        if (type == FulfillmentType.HOME_DELIVERY && request.getDeliveryAddress() == null) {
            throw new BusinessException("Delivery address is required for HOME_DELIVERY orders");
        }
        if (type == FulfillmentType.STORE_PICKUP) {
            if (request.getPickupSlot() == null) {
                throw new BusinessException("Pickup slot is required for STORE_PICKUP orders");
            }
            if (request.getPickupSlot().isBefore(LocalDateTime.now())) {
                throw new BusinessException("Pickup slot must be a future date and time");
            }
        }
    }

    private DeliveryAddress mapDeliveryAddress(PlaceOrderRequest request) {
        var addr = request.getDeliveryAddress();
        DeliveryAddress da = new DeliveryAddress();
        da.setStreet(addr.getStreet());
        da.setCity(addr.getCity());
        da.setState(addr.getState());
        da.setPincode(addr.getPincode());
        da.setLandmark(addr.getLandmark());
        return da;
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> itemResponses = o.getItems() == null ? List.of() :
                o.getItems().stream().map(this::toItemResponse).toList();

        OrderResponse.OrderResponseBuilder builder = OrderResponse.builder()
                .id(o.getId())
                .fulfillmentType(o.getFulfillmentType() != null ? o.getFulfillmentType().name() : null)
                .status(o.getStatus() != null ? o.getStatus().name() : null)
                .pickupSlot(o.getPickupSlot())
                .subtotal(o.getSubtotal())
                .deliveryCharge(o.getDeliveryCharge())
                .totalAmount(o.getTotalAmount())
                .items(itemResponses)
                .itemCount(itemResponses.size())
                .notes(o.getNotes())
                .deliveredAt(o.getDeliveredAt())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt());

        if (o.getUser() != null) {
            builder.userId(o.getUser().getId())
                   .customerName(o.getUser().getName())
                   .customerEmail(o.getUser().getEmail());
        }

        if (o.getDeliveryAddress() != null) {
            DeliveryAddress da = o.getDeliveryAddress();
            builder.deliveryStreet(da.getStreet())
                   .deliveryCity(da.getCity())
                   .deliveryState(da.getState())
                   .deliveryPincode(da.getPincode())
                   .deliveryLandmark(da.getLandmark());
        }

        return builder.build();
    }

    private OrderItemResponse toItemResponse(OrderItem i) {
        return OrderItemResponse.builder()
                .id(i.getId())
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productName(i.getProductName())
                .productUnit(i.getProductUnit())
                .unitPrice(i.getUnitPrice())
                .quantity(i.getQuantity())
                .totalPrice(i.getTotalPrice())
                .returned(i.isReturned())
                .build();
    }
}
