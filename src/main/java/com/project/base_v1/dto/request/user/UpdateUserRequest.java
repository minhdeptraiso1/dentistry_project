package com.project.base_v1.dto.request.user;

import com.project.base_v1.enums.UserRole;

public record UpdateUserRequest(
        
        String password,
        UserRole role,
        Boolean enabled
) {
}