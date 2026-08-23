package edu.demart_api.repository;

import edu.demart_api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndActiveTrue(Long id);

    // ─── Public Browse / Search / Filter ─────────────────────────────────────

    /**
     * Search products when search term is provided.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                   LOWER(p.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:inStockOnly = false OR p.stockQuantity > 0)
            """)
    Page<Product> searchProducts(@Param("search") String search,
                                 @Param("categoryId") Long categoryId,
                                 @Param("inStockOnly") boolean inStockOnly,
                                 Pageable pageable);

    /**
     * Category & stock filtering when no search term is present.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:inStockOnly = false OR p.stockQuantity > 0)
            """)
    Page<Product> findAllWithCategoryAndStock(@Param("categoryId") Long categoryId,
                                               @Param("inStockOnly") boolean inStockOnly,
                                               Pageable pageable);

    // ─── Admin / Staff ────────────────────────────────────────────────────────

    /** All products (including inactive) — for admin management view */
    Page<Product> findAll(Pageable pageable);

    /**
     * Low stock alert — products whose stockQuantity is at or below their own
     * minStockAlert threshold.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND p.stockQuantity > 0
              AND p.stockQuantity <= p.minStockAlert
            ORDER BY p.stockQuantity ASC
            """)
    List<Product> findLowStockProducts();

    /** Out-of-stock products — for admin restocking dashboard */
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();

    // ─── Atomic Stock Update ──────────────────────────────────────────────────

    @Modifying
    @Query("""
            UPDATE Product p
            SET p.stockQuantity = p.stockQuantity - :quantity
            WHERE p.id = :productId
              AND p.stockQuantity >= :quantity
            """)
    int decrementStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying
    @Query("""
            UPDATE Product p
            SET p.stockQuantity = p.stockQuantity + :quantity
            WHERE p.id = :productId
            """)
    int incrementStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
