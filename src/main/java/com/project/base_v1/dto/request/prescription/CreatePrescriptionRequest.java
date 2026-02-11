package com.project.base_v1.dto.request.prescription;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreatePrescriptionRequest(
        @NotNull UUID medicalRecordId,
        String note,
        List<CreatePrescriptionItemRequest> items
) {
}
