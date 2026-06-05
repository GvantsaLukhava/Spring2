package com.inventory.api.repository;

import com.inventory.api.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Category database operations.
 *
 * HOW SPRING DATA JPA WORKS:
 * By extending JpaRepository<Category, Long>, Spring auto-generates:
 *   - findAll(), findById(), save(), deleteById(), count(), existsById()...
 *   and much more - all without you writing any SQL!
 *
 * DERIVED QUERY METHODS:
 * Spring reads your method name and generates the SQL automatically.
 * Naming rules: findBy + FieldName + Condition
 *
 * Examples:
 *   findByActive(true)           → SELECT * FROM categories WHERE active = true
 *   findByNameContaining("ele")  → SELECT * FROM categories WHERE name LIKE '%ele%'
 *   findByActiveAndName(...)     → WHERE active = ? AND name = ?
 *
 * The Page<T> + Pageable pattern implements pagination automatically.
 * You pass in a Pageable (page number, size, sort) and get back a Page
 * which contains the data + total count + page metadata.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Returns paginated list of all active or inactive categories
    // SQL: SELECT * FROM categories WHERE active = ? LIMIT ? OFFSET ?
    Page<Category> findByActive(Boolean active, Pageable pageable);

    // For duplicate name check before creating
    boolean existsByNameIgnoreCase(String name);

    // For duplicate name check when updating (exclude current entity)
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // Find by name (case-insensitive) - useful for filtering
    Optional<Category> findByNameIgnoreCase(String name);

    // Paginated findAll (Spring provides this via JpaRepository, but explicit is clearer)
    Page<Category> findAll(Pageable pageable);
}
