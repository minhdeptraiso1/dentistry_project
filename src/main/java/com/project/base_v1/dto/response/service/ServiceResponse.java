package com.project.base_v1.dto.response.service;

import com.project.base_v1.enums.ServiceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String code,
        String name,
        ServiceType type,
        String category,
        String description,
        BigDecimal basePrice,
        String unit,
        Integer durationMin,
        boolean active,
        List<ServiceStepResponse> steps,
        Instant createdAt,
        Instant updatedAt
) {
}
