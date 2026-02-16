package com.project.base_v1.dto.response.invoice;

import com.project.base_v1.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceSummaryResponse(
        UUID id,
        String invoiceCode,

        UUID patientId,
        String patientCode,
        String patientName,

        UUID cashierId,
        String cashierUsername,

        InvoiceStatus status,

        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,

        Instant issuedAt,
        Instant paidAt,

        Instant createdAt
) {
}
