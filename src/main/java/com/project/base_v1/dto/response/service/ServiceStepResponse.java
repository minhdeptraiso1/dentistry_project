package com.project.base_v1.dto.response.service;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceStepResponse(
        UUID id,
        Integer stepNo,
        String stepName,
        String stepDesc,
        BigDecimal price,
        Integer quantity
) {
}
