package com.project.base_v1.dto.request.user;

import com.project.base_v1.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(

        @NotBlank
        String username,

        @Email
        String email,

        @NotNull
        String password,

        @NotNull
        String name,

        String img,

        @NotNull
        UserRole role

) {
}

