package com.project.base_v1.dto.response.user;

import com.project.base_v1.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
        @Schema(example = "admin")
        String username,

        @Schema(example = "admin@example.com")
        String email,


        @Schema(example = "true")
        Boolean enabled,

        @Schema(example = "ADMIN")
        UserRole role
) {
}
