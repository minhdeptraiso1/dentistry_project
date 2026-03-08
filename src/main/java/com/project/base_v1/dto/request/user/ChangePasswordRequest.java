package com.project.base_v1.dto.request.user;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {
}