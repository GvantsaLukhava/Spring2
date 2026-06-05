package com.inventory.api.controller;

import com.inventory.api.dto.request.CategoryRequest;
import com.inventory.api.dto.response.CategoryResponse;
import com.inventory.api.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Category endpoints.
 *
 * THE CONTROLLER'S JOB:
 * - Map HTTP methods + paths to Java methods
 * - Extract data from requests (path variables, query params, request body)
 * - Call the service layer
 * - Return appropriate HTTP status codes
 * - It should NOT contain business logic - that belongs in the service
 *
 * @RestController = @Controller + @ResponseBody
 *   Automatically serializes return values to JSON.
 *
 * @RequestMapping("/api/categories") - base path for all methods in this class
 *
 * HTTP STATUS CODES (the right ones matter!):
 *   200 OK          - successful GET, PUT
 *   201 Created     - successful POST (new resource created)
 *   204 No Content  - successful DELETE (nothing to return)
 *   400 Bad Request - validation failed
 *   404 Not Found   - resource doesn't exist
 *   409 Conflict    - duplicate resource
 *
 * PAGINATION via Pageable:
 *   Spring automatically reads these query params:
 *   ?page=0&size=10&sort=name,asc
 *   @PageableDefault sets defaults when not provided.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Returns ALL categories (active + inactive) with pagination.
     * Use case: admin panel showing all categories for management.
     *
     * Example: GET /api/categories?page=0&size=10&sort=name,asc
     */
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }

    /**
     * GET /api/categories/active
     * Returns ONLY active categories with pagination.
     * Use case: customer-facing storefront - only show live categories.
     *
     * BUSINESS USE CASE FOR ACTIVE/INACTIVE:
     * Imagine a clothing store. They have a "Winter Coats" category.
     * In summer, they deactivate it so it doesn't show on the website,
     * but all the coat products and sales history still exist in the database.
     * In October, they reactivate it. No data was lost or recreated.
     * This is "soft delete" - safer and reversible vs hard delete.
     */
    @GetMapping("/active")
    public ResponseEntity<Page<CategoryResponse>> getActiveCategories(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(categoryService.getActiveCategories(pageable));
    }

    /**
     * GET /api/categories/{id}
     * Returns a single category by ID.
     *
     * @PathVariable extracts {id} from the URL path.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * POST /api/categories
     * Creates a new category.
     *
     * @RequestBody: deserializes JSON body into CategoryRequest object.
     * @Valid: triggers validation of all annotations on CategoryRequest fields.
     *        If validation fails, Spring throws MethodArgumentNotValidException
     *        which our GlobalExceptionHandler catches → returns 400 with field errors.
     *
     * Returns 201 Created + the created resource.
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/categories/{id}
     * Updates an existing category (full replacement).
     *
     * REST convention: PUT = full update (all fields required)
     *                  PATCH = partial update (only provided fields change)
     * We implement PUT here for simplicity.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    /**
     * DELETE /api/categories/{id}
     * Deletes a category (and all its products due to CascadeType.ALL).
     * Returns 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/categories/{id}/toggle-active
     * Toggles active/inactive without a full update payload.
     * Clean and explicit - the URL communicates intent.
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<CategoryResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.toggleActive(id));
    }
}
