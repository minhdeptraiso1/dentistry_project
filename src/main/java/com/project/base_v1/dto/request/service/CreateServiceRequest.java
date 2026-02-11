package com.project.base_v1.dto.request.service;

import com.project.base_v1.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateServiceRequest(
        @NotBlank String name,
        @NotNull ServiceType type,
        String category,
        String description,
        @NotNull BigDecimal basePrice,
        String unit,
        Integer durationMin,
        List<CreatePackageStepRequest> steps // chỉ dùng khi type=PACKAGE
) {
}
