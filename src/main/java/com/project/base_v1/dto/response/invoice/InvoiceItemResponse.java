package com.project.base_v1.dto.response.invoice;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItemResponse(
        UUID id,
        UUID serviceId,
        String serviceCode,
        String itemName,
        String serviceType,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal lineTotal,
        String note
) {
}
