package edu.demart_api.controller;

import edu.demart_api.dto.request.CategoryRequest;
import edu.demart_api.dto.response.ApiResponse;
import edu.demart_api.dto.response.CategoryResponse;
import edu.demart_api.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Category endpoints.
 *
 * Public  : GET /api/v1/categories, GET /api/v1/categories/{id}
 * Admin   : POST / PUT / DELETE /api/v1/admin/categories/**
 */
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ─── Public ──────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success("Categories fetched", categoryService.getAllActive()));
    }

    @GetMapping("/api/v1/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Category fetched", categoryService.getById(id)));
    }

    // ─── Admin ───────────────────────────────────────────────────────────────

    /** Full list including inactive — for admin management table */
    @GetMapping("/api/v1/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("All categories fetched", categoryService.getAll()));
    }

    @PostMapping("/api/v1/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", created));
    }

    @PutMapping("/api/v1/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.update(id, request)));
    }

    /** Soft delete — hides category and its products from public */
    @DeleteMapping("/api/v1/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        categoryService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Category deactivated", null));
    }

    @PatchMapping("/api/v1/admin/categories/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        categoryService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Category activated", null));
    }
}
