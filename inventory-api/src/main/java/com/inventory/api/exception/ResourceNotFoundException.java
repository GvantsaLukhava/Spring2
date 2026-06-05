package com.inventory.api.exception;

/**
 * Thrown when a requested resource is not found in the database.
 * Maps to HTTP 404 Not Found.
 *
 * We extend RuntimeException (unchecked) so callers don't need try/catch.
 * The GlobalExceptionHandler intercepts it and returns the right HTTP response.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
