package com.project.base_v1.dto.response.core;

public record ErrorResponseSever(
        int code,
        String message
) {
}
