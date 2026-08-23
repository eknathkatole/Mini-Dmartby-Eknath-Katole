package edu.demart_api.service.impl;

import edu.demart_api.dto.request.ProductRequest;
import edu.demart_api.dto.request.StockUpdateRequest;
import edu.demart_api.dto.response.PageResponse;
import edu.demart_api.dto.response.ProductResponse;
import edu.demart_api.entity.Category;
import edu.demart_api.entity.Product;
import edu.demart_api.entity.Unit;
import edu.demart_api.exception.BusinessException;
import edu.demart_api.exception.ResourceNotFoundException;
import edu.demart_api.repository.CategoryRepository;
import edu.demart_api.repository.ProductRepository;
import edu.demart_api.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                               CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // ─── Public ──────────────────────────────────────────────────────────────

    @Override
    public PageResponse<ProductResponse> getAll(String search, Long categoryId,
                                                 boolean inStockOnly, Pageable pageable) {
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<Product> page;
        if (cleanSearch != null) {
            page = productRepository.searchProducts(cleanSearch, categoryId, inStockOnly, pageable);
        } else {
            page = productRepository.findAllWithCategoryAndStock(categoryId, inStockOnly, pageable);
        }
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public ProductResponse getById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

    // ─── Admin ───────────────────────────────────────────────────────────────

    @Override
    public PageResponse<ProductResponse> getAllAdmin(Pageable pageable) {
        return PageResponse.of(
                productRepository.findAll(pageable).map(this::toResponse)
        );
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = findCategoryOrThrow(request.getCategoryId());
        Unit unit = parseUnit(request.getUnit());
        validatePricing(request.getMrpPrice(), request.getSellingPrice());

        Product product = new Product();
        applyRequestToProduct(product, request, category, unit);

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);
        Category category = findCategoryOrThrow(request.getCategoryId());
        Unit unit = parseUnit(request.getUnit());
        validatePricing(request.getMrpPrice(), request.getSellingPrice());

        applyRequestToProduct(product, request, category, unit);

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Product product = findProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        Product product = findProductOrThrow(id);
        product.setActive(true);
        productRepository.save(product);
    }

    // ─── Stock Management ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductResponse updateStock(Long id, StockUpdateRequest request) {
        Product product = findProductOrThrow(id);
        int qty = request.getQuantity();

        switch (request.getOperation().toUpperCase()) {
            case "ADD" -> product.setStockQuantity(product.getStockQuantity() + qty);
            case "SUBTRACT" -> {
                int newQty = product.getStockQuantity() - qty;
                if (newQty < 0) {
                    throw new BusinessException(
                            "Cannot subtract " + qty + " units — only " + product.getStockQuantity() + " in stock");
                }
                product.setStockQuantity(newQty);
            }
            case "SET" -> product.setStockQuantity(qty);
            default -> throw new BusinessException(
                    "Invalid stock operation: '" + request.getOperation() + "'. Use ADD, SUBTRACT, or SET");
        }

        return toResponse(productRepository.save(product));
    }

    @Override
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private void applyRequestToProduct(Product product, ProductRequest request,
                                        Category category, Unit unit) {
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setUnit(unit);
        product.setMrpPrice(request.getMrpPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setMinStockAlert(request.getMinStockAlert() != null ? request.getMinStockAlert() : 10);
        product.setImageUrl(request.getImageUrl());
        product.setActive(true);
    }

    private void validatePricing(BigDecimal mrp, BigDecimal selling) {
        if (selling.compareTo(mrp) > 0) {
            throw new BusinessException(
                    "Selling price (" + selling + ") cannot exceed MRP (" + mrp + ")");
        }
    }

    private Unit parseUnit(String unitStr) {
        try {
            return Unit.valueOf(unitStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    "Invalid unit: '" + unitStr + "'. Allowed: KG, GRAMS, LITERS, ML, PIECES, DOZEN, PACK");
        }
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product p) {
        double discountPercent = 0.0;
        if (p.getMrpPrice() != null && p.getMrpPrice().compareTo(BigDecimal.ZERO) > 0
                && p.getSellingPrice() != null) {
            discountPercent = p.getMrpPrice()
                    .subtract(p.getSellingPrice())
                    .divide(p.getMrpPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .unit(p.getUnit() != null ? p.getUnit().name() : null)
                .mrpPrice(p.getMrpPrice())
                .sellingPrice(p.getSellingPrice())
                .discountPercent(discountPercent)
                .stockQuantity(p.getStockQuantity())
                .minStockAlert(p.getMinStockAlert())
                .inStock(p.isInStock())
                .lowStock(p.isLowStock())
                .imageUrl(p.getImageUrl())
                .active(p.isActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
