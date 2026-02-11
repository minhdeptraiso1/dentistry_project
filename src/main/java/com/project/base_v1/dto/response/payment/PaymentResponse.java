package com.project.base_v1.dto.response.payment;

import com.project.base_v1.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        PaymentMethod method,
        BigDecimal amount,
        Instant paidAt,
        String reference,
        String note
) {
}
