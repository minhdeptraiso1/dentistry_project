package com.project.base_v1.dto.request.user;


import com.project.base_v1.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserSearchRequest(

        @Schema(example = "admin")
        String keyword,

        @Schema(example = "ADMIN")
        UserRole role,

        @Schema(example = "true")
        Boolean enabled
) {
}