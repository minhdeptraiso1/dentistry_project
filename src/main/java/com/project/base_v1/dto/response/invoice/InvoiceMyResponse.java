package com.project.base_v1.dto.response.invoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InvoiceMyResponse(
        UUID id,
        String invoiceCode,

        UUID patientId,
        String patientCode,
        String patientName,

        BigDecimal totalAmount,
        BigDecimal paidAmount,

        String status,
        Instant issuedAt,

        List<InvoiceItemResponse> items
) {
}