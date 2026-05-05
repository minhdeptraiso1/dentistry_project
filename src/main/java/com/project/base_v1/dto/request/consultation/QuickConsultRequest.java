package com.project.base_v1.dto.request.consultation;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record QuickConsultRequest(
        @NotBlank(message = "Nội dung tư vấn không được để trống")
        String message,

        List<ChatMessageRequest> conversation
) {
}