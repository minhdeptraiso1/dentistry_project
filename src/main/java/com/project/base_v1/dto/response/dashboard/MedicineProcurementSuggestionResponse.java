package com.project.base_v1.dto.response.dashboard;

import java.util.UUID;

public record MedicineProcurementSuggestionResponse(
        UUID medicineId,
        String medicineCode,
        String medicineName,
        Integer currentStock,
        Integer nearExpiryQty,
        Integer monthlyDispensedQty,
        Integer suggestedQuantity,
        String priority,
        String reason
) {
}