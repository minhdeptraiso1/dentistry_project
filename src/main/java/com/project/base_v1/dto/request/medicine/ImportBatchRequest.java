package com.project.base_v1.dto.request.medicine;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ImportBatchRequest(
        @NotNull UUID medicineId,
        String batchNo,
        @NotNull LocalDate importDate,
        LocalDate expiryDate,
        @NotNull BigDecimal importPrice,
        @NotNull Integer quantityIn
) {
}
