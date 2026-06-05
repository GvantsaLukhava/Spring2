package com.inventory.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category entity - represents a product category (e.g. "Electronics", "Furniture").
 *
 * DATABASE RELATION EXPLAINED:
 * This is the "ONE" side of a One-to-Many relationship.
 * One Category can have MANY Products.
 * The foreign key (category_id) lives in the products table, not here.
 *
 * @OneToMany(mappedBy = "category") tells JPA:
 *   - "I have many Products"
 *   - "the 'category' field in Product owns this relationship"
 *   - mappedBy avoids creating a useless join table
 *
 * cascade = CascadeType.ALL means: if we delete a category, delete its products too
 * orphanRemoval = true means: if a product is removed from this list, delete it from DB
 */
@Entity
@Table(name = "categories")
@Data                   // Lombok: generates getters, setters, toString, equals, hashCode
@Builder                // Lombok: gives us a builder pattern: Category.builder().name("X").build()
@NoArgsConstructor      // Lombok: generates no-args constructor (required by JPA)
@AllArgsConstructor     // Lombok: generates all-args constructor (used by @Builder)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment in DB
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * ACTIVE FLAG - Business use case:
     * Deactivating a category means "we no longer sell items in this category"
     * but we keep historical data (orders, products) intact.
     * This is called "soft delete" - safer than hard deleting in production systems.
     *
     * Real-world example: A store stops selling "VHS Tapes" as a category.
     * Instead of deleting (which could break order history), we set active=false.
     * Active categories appear in the storefront; inactive ones don't.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // The "ONE" side - mapped by the "category" field in Product
    // FetchType.LAZY = don't load products from DB unless explicitly requested (better performance)
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    // JPA lifecycle callbacks - automatically set timestamps
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
