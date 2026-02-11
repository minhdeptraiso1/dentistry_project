package com.project.base_v1.dto.request.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePackageStepRequest(
        @NotNull Integer stepNo,
        @NotBlank String stepName,
        String stepDesc,
        @NotNull BigDecimal price,
        @NotNull Integer quantity
) {
}
