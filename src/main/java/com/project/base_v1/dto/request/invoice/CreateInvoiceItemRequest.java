package com.project.base_v1.dto.request.invoice;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInvoiceItemRequest(
        UUID serviceId,              // optional
        @NotNull String itemName,
        String serviceCode,
        String serviceType,

        Integer quantity,
        @NotNull BigDecimal unitPrice,
        BigDecimal discountAmount,
        String note
) {
}
