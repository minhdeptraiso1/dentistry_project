package com.project.base_v1.dto.request.medicine;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record SetMedicinePriceRequest(
        @NotNull @PositiveOrZero BigDecimal newPrice,
        String reason
) {
}
