package com.project.base_v1.dto.response.payment;

public record VnPayReturnResponse(
        boolean success,
        boolean verified,
        String message,
        String invoiceId,
        String responseCode
) {
}