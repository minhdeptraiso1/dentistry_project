package com.project.base_v1.dto.response.consultation;

import java.util.UUID;

public record SuggestedServiceResponse(
        UUID serviceId,
        String title,
        String description,
        String reason
) {
}