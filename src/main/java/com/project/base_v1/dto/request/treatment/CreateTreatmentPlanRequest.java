package com.project.base_v1.dto.request.treatment;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateTreatmentPlanRequest(
        @NotNull UUID medicalRecordId,
        String note,
        List<CreateTreatmentItemRequest> items
) {
}
