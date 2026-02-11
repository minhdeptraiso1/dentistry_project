package com.project.base_v1.dto.response.medicine;

import java.util.UUID;

public record MedicineResponse(
        UUID id,
        String code,
        String name,
        String ingredient,
        String unit,
        String usageGuide,
        boolean active
) {
}
