package com.inventory.api.service;

import com.inventory.api.dto.request.CategoryRequest;
import com.inventory.api.dto.response.CategoryResponse;
import com.inventory.api.exception.DuplicateResourceException;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.mapper.CategoryMapper;
import com.inventory.api.model.Category;
import com.inventory.api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer - contains all business logic for Category operations.
 *
 * THE SERVICE LAYER'S JOB:
 * - Validate business rules (e.g. no duplicate names)
 * - Orchestrate repositories (fetch, modify, save)
 * - Map between entities and DTOs
 * - Handle transactions
 *
 * @Service marks this as a Spring-managed service bean.
 *
 * @Transactional: all DB operations in a method run in one transaction.
 * If anything fails, ALL changes are rolled back - prevents partial data.
 * readOnly=true on queries: hint to DB to optimize for reads (no write locks).
 *
 * @RequiredArgsConstructor (Lombok): generates a constructor for all final fields.
 * This is constructor injection - the recommended way to inject dependencies.
 *
 * @Slf4j (Lombok): provides a `log` object (Logger) for logging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * GET ALL categories (paginated)
     * @Transactional(readOnly=true) - read-only transaction, slightly faster
     */
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        log.info("Fetching all categories, page: {}", pageable.getPageNumber());
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
        // .map() transforms each Category in the page to CategoryResponse
        // categoryMapper::toResponse is a method reference (shorthand for c -> categoryMapper.toResponse(c))
    }

    /**
     * GET ONLY ACTIVE categories (paginated)
     * Business use case: storefront shows only active categories.
     */
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getActiveCategories(Pageable pageable) {
        log.info("Fetching active categories");
        return categoryRepository.findByActive(true, pageable)
                .map(categoryMapper::toResponse);
    }

    /**
     * GET single category by ID
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findByIdOrThrow(id);
        return categoryMapper.toResponse(category);
    }

    /**
     * CREATE new category
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Business rule: no two categories with the same name
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException(
                    "Category with name '" + request.getName() + "' already exists");
        }

        Category category = categoryMapper.toEntity(request);
        Category saved = categoryRepository.save(category);
        log.info("Created category with id: {}", saved.getId());
        return categoryMapper.toResponse(saved);
    }

    /**
     * UPDATE existing category
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findByIdOrThrow(id);

        // Check name uniqueness, but exclude the current category (allow keeping same name)
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException(
                    "Category with name '" + request.getName() + "' already exists");
        }

        categoryMapper.updateEntity(category, request);
        Category updated = categoryRepository.save(category);
        log.info("Updated category with id: {}", id);
        return categoryMapper.toResponse(updated);
    }

    /**
     * DELETE category by ID (hard delete)
     * Note: because of CascadeType.ALL, this also deletes all products in this category.
     * In a real system you might want to prevent deleting categories with products.
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findByIdOrThrow(id);
        categoryRepository.delete(category);
        log.info("Deleted category with id: {}", id);
    }

    /**
     * TOGGLE active status (convenience method for soft enable/disable)
     */
    @Transactional
    public CategoryResponse toggleActive(Long id) {
        Category category = findByIdOrThrow(id);
        category.setActive(!category.getActive());  // flip the flag
        Category saved = categoryRepository.save(category);
        log.info("Toggled active status for category id: {} -> {}", id, saved.getActive());
        return categoryMapper.toResponse(saved);
    }

    // Private helper - DRY principle: "Don't Repeat Yourself"
    // Used in every method that needs to fetch by ID
    private Category findByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        // orElseThrow: if Optional is empty, throw the exception
    }
}
