package com.project.base_v1.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.core.ErrorResponseSever;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleBusiness(BusinessException ex) {

        ErrorCode code = ex.getErrorCode();

        return ResponseEntity
                .status(code.status())
                .body(new ApiResponseSever<>(
                        false,
                        null,
                        new ErrorResponseSever(code.code(), code.message())
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseSever<Void>> handleSystem(Exception ex) {

        ex.printStackTrace();

        ErrorCode code = ErrorCode.SYSTEM_ERROR;

        return ResponseEntity
                .status(code.status())
                .body(new ApiResponseSever<>(
                        false,
                        null,
                        new ErrorResponseSever(code.code(), code.message())
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        ErrorCode code = ErrorCode.BAD_REQUEST;

        return ResponseEntity
                .status(code.status())
                .body(new ApiResponseSever<>(
                        false,
                        null,
                        new ErrorResponseSever(code.code(), message)
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleDataIntegrity(
            DataIntegrityViolationException ex
    ) {
        ErrorCode code = ErrorCode.BAD_REQUEST;

        String message = "Database constraint violation";

        if (ex.getMostSpecificCause() != null) {
            message = ex.getMostSpecificCause().getMessage();
        }

        return ResponseEntity
                .status(code.status())
                .body(new ApiResponseSever<>(
                        false,
                        null,
                        new ErrorResponseSever(code.code(), message)
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseSever<Void>> handleJsonParse(
            HttpMessageNotReadableException ex
    ) {
        String message = "Invalid request body";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            // bắt riêng lỗi enum
            if (ife.getTargetType().isEnum()) {
                message = "Invalid value for field '" +
                        ife.getPath().get(0).getFieldName() +
                        "'. Allowed values: " +
                        java.util.Arrays.toString(ife.getTargetType().getEnumConstants());
            }
        }

        ErrorCode code = ErrorCode.BAD_REQUEST;

        return ResponseEntity
                .status(code.status())
                .body(new ApiResponseSever<>(
                        false,
                        null,
                        new ErrorResponseSever(code.code(), message)
                ));
    }
}
