package com.project.base_v1.dto.response.medicine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MedicineBatchResponse(
        UUID id,
        UUID medicineId,
        String medicineCode,
        String medicineName,
        String batchNo,
        LocalDate importDate,
        LocalDate expiryDate,
        BigDecimal importPrice,
        Integer quantityIn,
        Integer quantityRemaining
) {
}

