package com.project.base_v1.dto.request.payment;

import java.util.UUID;

public record CreateVnPayPaymentRequest(
        UUID invoiceId,
        String bankCode,
        String language
) {
}