package com.project.base_v1.dto.request.consultation;

public record ChatMessageRequest(
        String role,
        String content
) {
}