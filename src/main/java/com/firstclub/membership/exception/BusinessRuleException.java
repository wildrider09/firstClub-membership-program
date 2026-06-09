package com.firstclub.membership.exception;

/**
 * Thrown when an operation is rejected by a business rule (e.g. subscribing
 * while already subscribed, or moving to a tier the user is not eligible for).
 * Maps to HTTP 409 / 422.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
