package com.project.base_v1.dto.request.medicine;

import jakarta.validation.constraints.NotBlank;

public record CreateMedicineRequest(
        @NotBlank String name,
        String ingredient,
        String unit,
        String usageGuide
) {
}
