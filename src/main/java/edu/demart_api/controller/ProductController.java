package edu.demart_api.controller;

import edu.demart_api.dto.request.ProductRequest;
import edu.demart_api.dto.request.StockUpdateRequest;
import edu.demart_api.dto.response.ApiResponse;
import edu.demart_api.dto.response.PageResponse;
import edu.demart_api.dto.response.ProductResponse;
import edu.demart_api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product endpoints.
 *
 * Public   : GET  /api/v1/products/**
 * Admin    : POST / PUT / DELETE  /api/v1/admin/products/**
 * Staff    : PATCH /api/v1/staff/products/{id}/stock
 */
@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ─── Public ──────────────────────────────────────────────────────────────

    /**
     * Paginated product listing with optional filters.
     *
     * Query params:
     *   search      – partial name / description match
     *   categoryId  – filter by category
     *   inStockOnly – when true, exclude out-of-stock products (default: false)
     *   page        – 0-based page number (default: 0)
     *   size        – items per page (default: 20, max: 50)
     *   sort        – field,direction e.g. sellingPrice,asc
     */
    @GetMapping("/api/v1/products")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean inStockOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        size = Math.min(size, 50); // cap at 50 per page
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<ProductResponse> result = productService.getAll(search, categoryId, inStockOnly, pageable);
        return ResponseEntity.ok(ApiResponse.success("Products fetched", result));
    }

    @GetMapping("/api/v1/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Product fetched", productService.getById(id)));
    }

    // ─── Admin ───────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(
                ApiResponse.success("All products fetched", productService.getAllAdmin(pageable)));
    }

    @PostMapping("/api/v1/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", created));
    }

    @PutMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Product updated", productService.update(id, request)));
    }

    @DeleteMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        productService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated", null));
    }

    @PatchMapping("/api/v1/admin/products/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        productService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Product activated", null));
    }

    // ─── Stock Management (Admin + Staff) ─────────────────────────────────────

    /**
     * Update product stock.
     * Body: { "quantity": 50, "operation": "ADD", "reason": "Received from supplier" }
     */
    @PatchMapping("/api/v1/staff/products/{id}/stock")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Stock updated", productService.updateStock(id, request)));
    }

    /** Staff/Admin alert dashboard — products running low */
    @GetMapping("/api/v1/staff/products/low-stock")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStock() {
        return ResponseEntity.ok(
                ApiResponse.success("Low stock products fetched", productService.getLowStockProducts()));
    }

    /** Staff/Admin — products completely out of stock */
    @GetMapping("/api/v1/staff/products/out-of-stock")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getOutOfStock() {
        return ResponseEntity.ok(
                ApiResponse.success("Out of stock products fetched", productService.getOutOfStockProducts()));
    }
}
