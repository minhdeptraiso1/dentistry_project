package com.project.base_v1.dto.request.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record SendTemplateEmailRequest(
        @Email
        @NotBlank
        String to,

        @NotBlank
        String subject,

        @NotBlank
        String template,

        Map<String, Object> model
) {
}