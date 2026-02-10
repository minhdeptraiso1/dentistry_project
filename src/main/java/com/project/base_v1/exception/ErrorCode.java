package com.project.base_v1.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ==== AUTH (1xxxx) ====
    INVALID_CREDENTIALS(10401, HttpStatus.UNAUTHORIZED, "Invalid username or password"),
    TOKEN_EXPIRED(10402, HttpStatus.UNAUTHORIZED, "Token expired"),
    TOKEN_REVOKED(10403, HttpStatus.UNAUTHORIZED, "Token revoked"),
    ACCESS_DENIED(10403, HttpStatus.FORBIDDEN, "Access denied"),
    BAD_REQUEST(10400, HttpStatus.BAD_REQUEST, "Bad request"),

    USER_NOT_FOUND(10404, HttpStatus.NOT_FOUND, "User not found"),
    RESOURCE_NOT_FOUND(11404, HttpStatus.NOT_FOUND, "Resource not found"),
    PATIENT_NOT_FOUND(12404, HttpStatus.NOT_FOUND, "Patient not found"),
    MEDICAL_RECORD_NOT_FOUND(13404, HttpStatus.NOT_FOUND, "Medical Record not found"),

    TOO_MANY_REQUESTS(10429, HttpStatus.TOO_MANY_REQUESTS, "Too many requests"),

    // ==== SYSTEM (5xxxx) ====
    SYSTEM_ERROR(10500, HttpStatus.INTERNAL_SERVER_ERROR, "System error");

    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
