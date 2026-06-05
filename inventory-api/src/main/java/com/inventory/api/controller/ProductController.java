package com.inventory.api.controller;

import com.inventory.api.dto.request.ProductRequest;
import com.inventory.api.dto.response.ProductResponse;
import com.inventory.api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST Controller for Product endpoints.
 *
 * FILTERING DESIGN:
 * All filters are optional @RequestParam (query string parameters).
 * The client can combine any filters:
 *   GET /api/products?categoryId=1&active=true&minPrice=10&maxPrice=100&page=0&size=20
 *
 * Spring automatically maps query params to method parameters.
 * required=false means the param is optional (defaults to null if not provided).
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products
     * Returns ALL products with optional filters + pagination.
     *
     * Filter examples:
     *   ?categoryId=1              → products in category 1
     *   ?active=true               → only active products
     *   ?minPrice=10&maxPrice=50   → products between $10 and $50
     *   ?minQty=1                  → in-stock products (qty >= 1)
     *   ?search=laptop             → products with "laptop" in the name
     *   ?categoryId=1&minQty=1     → combine filters freely
     *
     * Pagination:
     *   ?page=0&size=20&sort=price,asc
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minQty,
            @RequestParam(required = false) Integer maxQty,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(
                productService.getAllProducts(categoryId, active, minPrice, maxPrice, minQty, maxQty, search, pageable)
        );
    }

    /**
     * GET /api/products/active
     * Dedicated endpoint for active products only (no other filters).
     * Use case: customer-facing product listing page.
     */
    @GetMapping("/active")
    public ResponseEntity<Page<ProductResponse>> getActiveProducts(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(productService.getActiveProducts(pageable));
    }

    /**
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * POST /api/products
     * Creates a new product. Requires a valid categoryId in the request body.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/products/{id}
     * Full update of a product.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/products/{id}/toggle-active
     * Enables or disables a product without a full PUT payload.
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ProductResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleActive(id));
    }
}
