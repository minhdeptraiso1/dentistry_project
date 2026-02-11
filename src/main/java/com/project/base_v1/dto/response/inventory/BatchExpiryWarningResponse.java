package com.project.base_v1.dto.response.inventory;

import java.time.LocalDate;
import java.util.UUID;

public record BatchExpiryWarningResponse(
        UUID batchId,
        UUID medicineId,
        String medicineCode,
        String medicineName,
        String batchNo,
        LocalDate importDate,
        LocalDate expiryDate,
        Integer quantityRemaining,
        String warningType // EXPIRED / NEAR_EXPIRY
) {
}
