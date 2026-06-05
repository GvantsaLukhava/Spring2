package com.inventory.api.mapper;

import com.inventory.api.dto.request.CategoryRequest;
import com.inventory.api.dto.response.CategoryResponse;
import com.inventory.api.model.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper for Category - converts between Entity ↔ DTO.
 *
 * WHY A SEPARATE MAPPER CLASS?
 * Keeps conversion logic in one place.
 * If you add a field, you change the mapper (not scattered across services/controllers).
 *
 * Alternatives: MapStruct (annotation-based, generates code at compile time) or ModelMapper.
 * We use manual mapping here so you can see exactly what's happening.
 *
 * @Component marks this as a Spring bean so it can be @Autowired / injected anywhere.
 */
@Component
public class CategoryMapper {

    /**
     * Entity → Response DTO
     * Called when returning data to the client.
     */
    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                // products list is LAZY - safe to call .size() here if already loaded
                // We guard with null check for safety
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * Request DTO → New Entity
     * Called when creating a new category.
     */
    public Category toEntity(CategoryRequest request) {
        return Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
    }

    /**
     * Update existing Entity from Request DTO
     * Called on PUT - we modify the existing entity (keeps createdAt, id, etc.)
     */
    public void updateEntity(Category category, CategoryRequest request) {
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        // Note: we do NOT update createdAt here - @PreUpdate handles updatedAt
    }
}
