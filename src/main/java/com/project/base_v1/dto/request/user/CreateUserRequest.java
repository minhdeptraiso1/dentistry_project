package com.project.base_v1.dto.request.user;

import com.project.base_v1.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(

        @Schema(
                description = "Username",
                example = "admin"
        )
        @NotBlank(message = "Username must not be blank")
        String username,

        @Schema(
                description = "Email",
                example = "admin@example.com"
        )
        @Email(message = "Email is invalid")
        @NotBlank(message = "Email must not be blank")
        String email,


        @Schema(
                description = "Password",
                example = "123456"
        )
        @NotNull(message = "Password must not be null")
        String password,

        @Schema(
                description = "User role",
                example = "ADMIN"
        )
        @NotNull(message = "Role must not be null")
        UserRole role
) {
}

