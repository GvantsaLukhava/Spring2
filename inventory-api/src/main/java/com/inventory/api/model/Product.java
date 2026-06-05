package com.inventory.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity - represents an item stored in the warehouse.
 *
 * DATABASE RELATION EXPLAINED:
 * This is the "MANY" side of a One-to-Many (Category → Products) relationship.
 * Many Products belong to ONE Category.
 *
 * @ManyToOne - "I belong to one Category"
 * @JoinColumn(name = "category_id") - creates a foreign key column called "category_id"
 *   in the products table that references the categories table.
 *
 * Why BigDecimal for price? Never use float/double for money!
 * Float arithmetic: 0.1 + 0.2 = 0.30000000000000004 (rounding errors)
 * BigDecimal is exact: 0.1 + 0.2 = 0.3 exactly
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    // precision=10, scale=2 means: up to 99999999.99
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    // SKU = Stock Keeping Unit - unique identifier for each product variant
    @Column(name = "sku", unique = true, length = 100)
    private String sku;

    /**
     * ACTIVE FLAG - Business use case:
     * Products are deactivated (not deleted) when:
     * 1. They're discontinued but still appear in old orders
     * 2. They're temporarily out of stock and shouldn't appear on the storefront
     * 3. They're being reviewed/updated before going live
     *
     * API exposes two endpoints:
     *   GET /api/products         → all products (for internal management/admin)
     *   GET /api/products/active  → only active products (for storefront/customers)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * THE RELATIONSHIP - Foreign Key
     * @ManyToOne: many products → one category
     * @JoinColumn: this table holds the FK column "category_id"
     * FetchType.LAZY: load the Category from DB only when product.getCategory() is called
     *
     * In the DB this creates:
     *   products.category_id → references categories.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
