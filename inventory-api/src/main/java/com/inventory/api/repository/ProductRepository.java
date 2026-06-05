package com.inventory.api.repository;

import com.inventory.api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Repository for Product database operations.
 *
 * We use two approaches here to show you both options:
 * 1. Derived query methods (Spring reads the name, generates SQL)
 * 2. @Query with JPQL (you write the query, more flexibility)
 *
 * JPQL vs SQL:
 * - SQL operates on tables and columns:  SELECT * FROM products WHERE ...
 * - JPQL operates on entities and fields: SELECT p FROM Product p WHERE ...
 * JPQL is database-agnostic (works with Postgres, MySQL, H2, etc.)
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // --- Simple derived queries ---

    // Filter by active flag + paginate
    Page<Product> findByActive(Boolean active, Pageable pageable);

    // Filter by category ID (using the relationship)
    // Spring understands "category_id" through the @ManyToOne field named "category"
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Filter by active AND category
    Page<Product> findByActiveAndCategoryId(Boolean active, Long categoryId, Pageable pageable);

    // Check SKU uniqueness
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);

    // --- JPQL @Query for complex filtering ---
    /**
     * FILTERING ENDPOINT - supports multiple optional filters simultaneously.
     *
     * JPQL trick: (:param IS NULL OR p.field = :param)
     * When param is null (not provided), the condition is always true (filter ignored).
     * When param has a value, the condition filters normally.
     * This gives us one query that handles all filter combinations.
     *
     * This powers: GET /api/products?categoryId=1&active=true&minPrice=10&maxPrice=100
     */
    @Query("""
            SELECT p FROM Product p
            JOIN FETCH p.category c
            WHERE (:categoryId IS NULL OR c.id = :categoryId)
              AND (:active IS NULL OR p.active = :active)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:minQty IS NULL OR p.quantity >= :minQty)
              AND (:maxQty IS NULL OR p.quantity <= :maxQty)
              AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Product> findWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("active") Boolean active,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minQty") Integer minQty,
            @Param("maxQty") Integer maxQty,
            @Param("search") String search,
            Pageable pageable
    );
}
