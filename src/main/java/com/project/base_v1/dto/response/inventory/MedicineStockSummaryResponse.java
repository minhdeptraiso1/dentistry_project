package com.project.base_v1.dto.response.inventory;

import java.time.LocalDate;
import java.util.UUID;

public record MedicineStockSummaryResponse(
        UUID medicineId,
        String medicineCode,
        String medicineName,
        String unit,
        Integer totalRemaining,
        Integer totalBatches,
        LocalDate nearestExpiryDate,
        Integer expiredBatchesCount,
        Integer nearExpiryBatchesCount,
        boolean lowStock,
        boolean hasExpired,
        boolean hasNearExpiry
) {
}
