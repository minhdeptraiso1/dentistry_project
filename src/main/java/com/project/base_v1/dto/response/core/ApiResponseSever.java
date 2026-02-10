package com.project.base_v1.dto.response.core;

public record ApiResponseSever<T>(
        boolean success,
        T data,
        ErrorResponseSever error
) {

    public static <T> ApiResponseSever<T> ok(T data) {
        return new ApiResponseSever<>(true, data, null);
    }

    public static <T> ApiResponseSever<T> error(ErrorResponseSever error) {
        return new ApiResponseSever<>(false, null, error);
    }
}
