package com.inventory.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO returned to the client when they request a Category.
 *
 * We include productCount (derived data) instead of embedding all products.
 * This is a design choice: embedding full product lists here would cause
 * huge payloads. Clients who want products use GET /api/products?categoryId=X
 */
@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private int productCount;       // computed: how many products in this category
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
