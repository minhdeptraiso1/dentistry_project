package com.project.base_v1.dto.response.treatment;

import com.project.base_v1.enums.TreatmentItemStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record TreatmentItemResponse(
        UUID id,
        UUID serviceId,
        String serviceCode,
        String itemName,
        String serviceType,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal lineTotal,
        String toothNo,
        String toothSurface,
        TreatmentItemStatus status,
        String note
) {
}
