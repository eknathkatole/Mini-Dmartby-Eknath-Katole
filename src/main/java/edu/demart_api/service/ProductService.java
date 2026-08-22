package edu.demart_api.service;

import edu.demart_api.dto.request.ProductRequest;
import edu.demart_api.dto.request.StockUpdateRequest;
import edu.demart_api.dto.response.PageResponse;
import edu.demart_api.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    // ─── Public ──────────────────────────────────────────────────────────────

    /** Paginated product listing with optional search, category, and stock filters */
    PageResponse<ProductResponse> getAll(String search, Long categoryId,
                                         boolean inStockOnly, Pageable pageable);

    ProductResponse getById(Long id);

    // ─── Admin ───────────────────────────────────────────────────────────────

    /** Full admin listing (includes inactive products) */
    PageResponse<ProductResponse> getAllAdmin(Pageable pageable);

    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);

    void deactivate(Long id);   // soft delete — product hidden from public
    void activate(Long id);

    // ─── Stock Management (Admin + Staff) ────────────────────────────────────

    ProductResponse updateStock(Long id, StockUpdateRequest request);

    /** Returns products with stock at or below their minStockAlert */
    List<ProductResponse> getLowStockProducts();

    /** Returns products with stockQuantity = 0 */
    List<ProductResponse> getOutOfStockProducts();
}
