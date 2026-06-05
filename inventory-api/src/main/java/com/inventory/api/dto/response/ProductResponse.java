package com.inventory.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO returned to the client when they request a Product.
 *
 * We include categoryId AND categoryName as a convenience.
 * The client gets the relationship info without needing a second API call.
 */
@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String sku;
    private Boolean active;
    private Long categoryId;        // the FK value
    private String categoryName;    // denormalized for client convenience
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
