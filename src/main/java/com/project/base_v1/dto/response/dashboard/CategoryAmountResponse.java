package com.project.base_v1.dto.response.dashboard;

import java.math.BigDecimal;

public record CategoryAmountResponse(
        String category,
        BigDecimal amount
) {
}
