package com.project.base_v1.dto.request.medical_record;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateMedicalRecordRequest(
        @NotNull UUID patientId,
        @NotNull UUID doctorId,
        Instant visitDate, // nếu null => now()
        String symptom,
        String diagnosis,
        String note
) {
}
