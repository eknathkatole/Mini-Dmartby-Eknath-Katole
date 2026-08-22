package edu.demart_api.repository;

import edu.demart_api.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Used on create/update to prevent duplicate category names */
    boolean existsByNameIgnoreCase(String name);

    /** Used on update to prevent duplicate name but allow same record */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /** Public listing — only active categories */
    List<Category> findByActiveTrueOrderByNameAsc();

    Optional<Category> findByIdAndActiveTrue(Long id);

    /** Count active products inside a category (used for productCount in response) */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    long countActiveProducts(Long categoryId);
}
