package com.project.base_v1.dto.response.medical_record;

import java.time.Instant;
import java.util.UUID;

public record MedicalRecordResponse(
        UUID id,
        String recordCode,

        UUID patientId,
        String patientCode,
        String patientName,

        UUID doctorId,
        String doctorUsername,

        Instant visitDate,
        String symptom,
        String diagnosis,
        String note,

        Instant createdAt,
        Instant updatedAt
) {
}
