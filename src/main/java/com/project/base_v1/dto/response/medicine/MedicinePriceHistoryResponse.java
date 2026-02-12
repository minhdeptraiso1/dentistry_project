package com.project.base_v1.dto.response.medicine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MedicinePriceHistoryResponse(
        UUID id,
        UUID medicineId,
        String medicineCode,
        String medicineName,
        BigDecimal oldPrice,
        BigDecimal newPrice,
        String reason,
        Instant changedAt,
        String changedBy
) {
}
