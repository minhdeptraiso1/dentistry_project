package com.project.base_v1.dto.request.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendEmailRequest(
        @Email
        @NotBlank
        String to,

        @NotBlank
        String subject,

        @NotBlank
        String html
) {
}