package com.inventory.api.service;

import com.inventory.api.dto.request.ProductRequest;
import com.inventory.api.dto.response.ProductResponse;
import com.inventory.api.exception.DuplicateResourceException;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.mapper.ProductMapper;
import com.inventory.api.model.Category;
import com.inventory.api.model.Product;
import com.inventory.api.repository.CategoryRepository;
import com.inventory.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    /**
     * GET ALL products with optional filters (paginated)
     *
     * This single method handles all these request variants:
     *   GET /api/products                          → all products
     *   GET /api/products?categoryId=1             → by category
     *   GET /api/products?minPrice=10&maxPrice=50  → by price range
     *   GET /api/products?minQty=5                 → by quantity
     *   GET /api/products?search=laptop            → by name search
     *   GET /api/products?categoryId=1&active=true → combined filters
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(
            Long categoryId,
            Boolean active,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minQty,
            Integer maxQty,
            String search,
            Pageable pageable) {

        log.info("Fetching products with filters - categoryId: {}, active: {}, search: {}",
                categoryId, active, search);

        return productRepository.findWithFilters(
                        categoryId, active, minPrice, maxPrice, minQty, maxQty, search, pageable)
                .map(productMapper::toResponse);
    }

    /**
     * GET ONLY ACTIVE products (no other filters)
     * Dedicated endpoint: GET /api/products/active
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getActiveProducts(Pageable pageable) {
        return productRepository.findByActive(true, pageable)
                .map(productMapper::toResponse);
    }

    /**
     * GET single product
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findByIdOrThrow(id);
        return productMapper.toResponse(product);
    }

    /**
     * CREATE new product
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // Verify the category exists before creating the product
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        // SKU uniqueness check (SKU is optional but must be unique if provided)
        if (request.getSku() != null && !request.getSku().isBlank()) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new DuplicateResourceException(
                        "Product with SKU '" + request.getSku() + "' already exists");
            }
        }

        Product product = productMapper.toEntity(request, category);
        Product saved = productRepository.save(product);
        log.info("Created product with id: {}", saved.getId());
        return productMapper.toResponse(saved);
    }

    /**
     * UPDATE existing product
     */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findByIdOrThrow(id);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        // SKU uniqueness check - exclude current product
        if (request.getSku() != null && !request.getSku().isBlank()) {
            if (productRepository.existsBySkuAndIdNot(request.getSku(), id)) {
                throw new DuplicateResourceException(
                        "Product with SKU '" + request.getSku() + "' already exists");
            }
        }

        productMapper.updateEntity(product, request, category);
        Product updated = productRepository.save(product);
        log.info("Updated product with id: {}", id);
        return productMapper.toResponse(updated);
    }

    /**
     * DELETE product
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findByIdOrThrow(id);
        productRepository.delete(product);
        log.info("Deleted product with id: {}", id);
    }

    /**
     * TOGGLE active status - soft disable/enable a product
     */
    @Transactional
    public ProductResponse toggleActive(Long id) {
        Product product = findByIdOrThrow(id);
        product.setActive(!product.getActive());
        Product saved = productRepository.save(product);
        log.info("Toggled active status for product id: {} -> {}", id, saved.getActive());
        return productMapper.toResponse(saved);
    }

    private Product findByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
