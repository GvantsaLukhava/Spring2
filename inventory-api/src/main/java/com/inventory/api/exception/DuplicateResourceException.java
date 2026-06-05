package com.inventory.api.exception;

/**
 * Thrown when trying to create a resource that already exists (e.g., duplicate SKU).
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
