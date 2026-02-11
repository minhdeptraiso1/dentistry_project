package com.project.base_v1.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ===================== COMMON (101xxx) =====================
    BAD_REQUEST(101001, HttpStatus.BAD_REQUEST, "Bad request"),
    VALIDATION_FAILED(101002, HttpStatus.BAD_REQUEST, "Validation failed"),
    RESOURCE_NOT_FOUND(101003, HttpStatus.NOT_FOUND, "Resource not found"),
    TOO_MANY_REQUESTS(101004, HttpStatus.TOO_MANY_REQUESTS, "Too many requests"),

    // ===================== AUTH (102xxx) =====================
    INVALID_CREDENTIALS(102001, HttpStatus.UNAUTHORIZED, "Invalid username or password"),
    TOKEN_EXPIRED(102002, HttpStatus.UNAUTHORIZED, "Token expired"),
    TOKEN_REVOKED(102003, HttpStatus.UNAUTHORIZED, "Token revoked"),
    ACCESS_DENIED(102004, HttpStatus.FORBIDDEN, "Access denied"),
    TOKEN_INVALID(102005, HttpStatus.UNAUTHORIZED, "Invalid token"),

    // ===================== USER (103xxx) =====================
    USER_NOT_FOUND(103001, HttpStatus.NOT_FOUND, "User not found"),
    USERNAME_ALREADY_EXISTS(103002, HttpStatus.BAD_REQUEST, "Username already exists"),
    EMAIL_ALREADY_EXISTS(103003, HttpStatus.BAD_REQUEST, "Email already exists"),

    // ===================== PATIENT (104xxx) =====================
    PATIENT_NOT_FOUND(104001, HttpStatus.NOT_FOUND, "Patient not found"),
    PATIENT_PHONE_DUPLICATED(104002, HttpStatus.BAD_REQUEST, "Patient phone duplicated"),

    // ===================== MEDICAL RECORD (105xxx) =====================
    MEDICAL_RECORD_NOT_FOUND(105001, HttpStatus.NOT_FOUND, "Medical record not found"),
    DOCTOR_REQUIRED(105002, HttpStatus.BAD_REQUEST, "Doctor role required"),

    // ===================== SERVICE CATALOG (106xxx) =====================
    SERVICE_NOT_FOUND(106001, HttpStatus.NOT_FOUND, "Service not found"),
    PACKAGE_STEPS_REQUIRED(106002, HttpStatus.BAD_REQUEST, "Package steps required"),
    PACKAGE_STEP_NO_DUPLICATED(106003, HttpStatus.BAD_REQUEST, "Duplicate stepNo in package steps"),
    INVALID_STEP_NO(106004, HttpStatus.BAD_REQUEST, "Invalid stepNo"),
    INVALID_STEP_QUANTITY(106005, HttpStatus.BAD_REQUEST, "Invalid step quantity"),
    INVALID_STEP_PRICE(106006, HttpStatus.BAD_REQUEST, "Invalid step price"),
    SINGLE_SERVICE_SHOULD_NOT_HAVE_STEPS(106007, HttpStatus.BAD_REQUEST, "Single service should not contain package steps"),

    // ===================== SYSTEM (500xxx) =====================
    SYSTEM_ERROR(500001, HttpStatus.INTERNAL_SERVER_ERROR, "System error");

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
