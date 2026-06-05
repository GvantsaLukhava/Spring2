package com.inventory.api.mapper;

import com.inventory.api.dto.request.ProductRequest;
import com.inventory.api.dto.response.ProductResponse;
import com.inventory.api.model.Category;
import com.inventory.api.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .sku(product.getSku())
                .active(product.getActive())
                // Safely access the related category (may be LAZY-loaded)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public Product toEntity(ProductRequest request, Category category) {
        return Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .sku(request.getSku() != null ? request.getSku().trim() : null)
                .active(request.getActive() != null ? request.getActive() : true)
                .category(category)   // set the relationship!
                .build();
    }

    public void updateEntity(Product product, ProductRequest request, Category category) {
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setSku(request.getSku() != null ? request.getSku().trim() : null);
        product.setCategory(category);
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
    }
}
