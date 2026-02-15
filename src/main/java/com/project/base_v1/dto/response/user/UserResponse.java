package com.project.base_v1.dto.response.user;

import com.project.base_v1.enums.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
public record UserResponse(

        @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

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
