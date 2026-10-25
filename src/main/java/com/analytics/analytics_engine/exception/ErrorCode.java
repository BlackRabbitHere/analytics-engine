package com.analytics.analytics_engine.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_FAILED(
            HttpStatus.BAD_REQUEST,
            "Request validation failed"
    ),
    INVALID_REQUEST_BODY(
            HttpStatus.BAD_REQUEST,
            "Request body is missing or invalid"
    ),
    USERNAME_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Username already taken"
    ),
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "User not found"
    ),
    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
    ),
    LEAD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Lead not found"
    ),
    ORDER_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "Order already created"
    ),
    LEAD_USER_MISMATCH(
            HttpStatus.CONFLICT,
            "Lead and user mismatch"
    ),
    ORDER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Order not found"
    ),
    PROVIDER_REFERENCE_MISMATCH(
            HttpStatus.CONFLICT,
            "Provider Reference Mismatch"
            ),
    PAYMENT_CONFLICT(
            HttpStatus.CONFLICT,
            "Payment reference or order conflicts with an existing payment"
    ),

    PAYMENT_AMOUNT_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "Payment amount does not match the order amount"
    ),

    ORDER_NOT_PAYABLE(
            HttpStatus.CONFLICT,
            "Only a pending order can receive a new payment"
    );

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}