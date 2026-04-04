package com.project.base_v1.dto.response.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String title,
        String content,
        boolean read,
        Instant createdAt
) {
}