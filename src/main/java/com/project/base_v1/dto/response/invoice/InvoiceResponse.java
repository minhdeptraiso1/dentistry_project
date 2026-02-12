package com.project.base_v1.dto.response.invoice;

import com.project.base_v1.dto.response.payment.PaymentResponse;
import com.project.base_v1.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceCode,

        UUID patientId,
        String patientCode,
        String patientName,

        UUID cashierId,
        String cashierUsername,

        UUID treatmentPlanId,

        UUID prescriptionId,
        
        InvoiceStatus status,
        String note,

        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,

        Instant issuedAt,
        Instant paidAt,

        List<InvoiceItemResponse> items,
        List<PaymentResponse> payments,

        Instant createdAt,
        Instant updatedAt
) {
}
