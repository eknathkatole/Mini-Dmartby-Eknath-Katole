package edu.demart_api.service.impl;

import edu.demart_api.dto.request.CategoryRequest;
import edu.demart_api.dto.response.CategoryResponse;
import edu.demart_api.entity.Category;
import edu.demart_api.exception.BusinessException;
import edu.demart_api.exception.ResourceNotFoundException;
import edu.demart_api.repository.CategoryRepository;
import edu.demart_api.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ─── Public ──────────────────────────────────────────────────────────────

    @Override
    public List<CategoryResponse> getAllActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return toResponse(category);
    }

    // ─── Admin ───────────────────────────────────────────────────────────────

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Category already exists: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setActive(true);

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOrThrow(id);

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new BusinessException("Another category with this name already exists: " + request.getName());
        }

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Category category = findOrThrow(id);
        category.setActive(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        Category category = findOrThrow(id);
        category.setActive(true);
        categoryRepository.save(category);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        long productCount = categoryRepository.countActiveProducts(category.getId());
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .active(category.isActive())
                .productCount(productCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
