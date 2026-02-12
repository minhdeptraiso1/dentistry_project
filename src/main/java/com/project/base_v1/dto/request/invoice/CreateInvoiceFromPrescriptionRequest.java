package com.project.base_v1.dto.request.invoice;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInvoiceFromPrescriptionRequest(
        @NotNull UUID prescriptionId,
        BigDecimal markupRate,   // optional, default 1.2
        BigDecimal discountAmount,
        String note
) {
}
