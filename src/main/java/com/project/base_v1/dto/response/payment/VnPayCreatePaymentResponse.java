package com.project.base_v1.dto.response.payment;

public record VnPayCreatePaymentResponse(
        String code,
        String message,
        String paymentUrl
) {
}