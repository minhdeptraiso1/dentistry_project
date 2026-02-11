package com.project.base_v1.dto.request.treatment;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTreatmentItemRequest(
        @NotNull UUID serviceId,
        Integer quantity,
        BigDecimal unitPrice,        // nếu null => lấy basePrice của service
        BigDecimal discountAmount,   // default 0
        String toothNo,
        String toothSurface,
        String note
) {
}
