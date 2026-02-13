package com.project.base_v1.dto.response.dashboard;

import java.math.BigDecimal;

public record TimePointAmountResponse(
        String date,           // "2026-02-13"
        BigDecimal amount
) {
}
