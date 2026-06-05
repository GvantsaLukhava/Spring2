package com.inventory.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO (Data Transfer Object) for creating/updating a Category.
 *
 * WHY USE DTOs INSTEAD OF ENTITIES DIRECTLY?
 * 1. Security: entities may have fields you don't want exposed (e.g. internal flags)
 * 2. Validation: DTOs carry validation annotations; entities shouldn't
 * 3. Decoupling: your API contract is separate from your DB schema
 *    (you can change the DB without breaking the API and vice versa)
 * 4. Shape: the request shape might differ from the entity shape
 *
 * VALIDATION ANNOTATIONS (from spring-boot-starter-validation):
 * @NotBlank    - must not be null AND must contain non-whitespace characters
 * @NotNull     - just not null (empty string is ok)
 * @Size        - length constraints
 * @Min / @Max  - numeric range
 * These are checked automatically when @Valid is used in the controller.
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // active defaults to true if not provided - we handle this in the service
    private Boolean active;
}
