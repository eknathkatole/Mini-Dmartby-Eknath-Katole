package edu.demart_api.service;

import edu.demart_api.dto.request.CategoryRequest;
import edu.demart_api.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    // ─── Public ──────────────────────────────────────────────────────────────
    List<CategoryResponse> getAllActive();
    CategoryResponse getById(Long id);

    // ─── Admin ───────────────────────────────────────────────────────────────
    List<CategoryResponse> getAll();                              // includes inactive
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void deactivate(Long id);                                     // soft delete
    void activate(Long id);
}
