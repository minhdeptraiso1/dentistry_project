package com.project.base_v1.dto.response.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
