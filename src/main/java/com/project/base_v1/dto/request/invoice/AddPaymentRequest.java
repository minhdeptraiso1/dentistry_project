package com.project.base_v1.dto.request.payment;

import com.project.base_v1.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddPaymentRequest(
        @NotNull PaymentMethod method,
        @NotNull BigDecimal amount,
        String reference,
        String note
) {
}
