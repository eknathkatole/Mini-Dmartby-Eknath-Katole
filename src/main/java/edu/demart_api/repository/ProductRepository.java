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
     * Full search + filter for the public product listing.
     * - search: matches name or description (case-insensitive)
     * - categoryId: optional filter (null = all categories)
     * - inStockOnly: when true, only returns products with stockQuantity > 0
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (:search IS NULL OR
                   LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:inStockOnly = false OR p.stockQuantity > 0)
            """)
    Page<Product> findAllWithFilters(@Param("search") String search,
                                     @Param("categoryId") Long categoryId,
                                     @Param("inStockOnly") boolean inStockOnly,
                                     Pageable pageable);

    // ─── Admin / Staff ────────────────────────────────────────────────────────

    /** All products (including inactive) — for admin management view */
    Page<Product> findAll(Pageable pageable);

    /**
     * Low stock alert — products whose stockQuantity is at or below their own
     * minStockAlert threshold. Excludes out-of-stock (qty = 0) which are
     * already flagged separately.
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

    /**
     * Atomic in-place stock decrement — used during order placement.
     * The WHERE clause guards against going below 0 (optimistic stock check).
     * Returns the number of rows updated (0 = insufficient stock).
     */
    @Modifying
    @Query("""
            UPDATE Product p
            SET p.stockQuantity = p.stockQuantity - :quantity
            WHERE p.id = :productId
              AND p.stockQuantity >= :quantity
            """)
    int decrementStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Atomic in-place stock increment — used on order cancellation / return approval.
     */
    @Modifying
    @Query("""
            UPDATE Product p
            SET p.stockQuantity = p.stockQuantity + :quantity
            WHERE p.id = :productId
            """)
    int incrementStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
