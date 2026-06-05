package com.inventory.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for creating/updating a Product.
 *
 * Notice we accept categoryId (a Long) rather than a full Category object.
 * This is typical REST design - reference related resources by their ID.
 */
@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format invalid (max 8 digits, 2 decimal places)")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String sku;

    private Boolean active;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
