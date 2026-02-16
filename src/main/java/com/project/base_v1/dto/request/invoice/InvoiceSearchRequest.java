package com.project.base_v1.dto.request.invoice;

import com.project.base_v1.enums.InvoiceStatus;

import java.time.Instant;
import java.util.UUID;

public record InvoiceSearchRequest(
        UUID patientId,
        InvoiceStatus status,
        Instant fromDate,
        Instant toDate
) {
}
