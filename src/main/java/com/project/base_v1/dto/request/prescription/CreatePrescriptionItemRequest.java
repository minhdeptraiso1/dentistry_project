package com.project.base_v1.dto.request.prescription;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePrescriptionItemRequest(
        @NotNull UUID medicineId,
        @NotNull Integer quantity,
        String dosage,
        String note
) {
}
