package com.project.base_v1.dto.response.prescription;

import com.project.base_v1.enums.PrescriptionStatus;

import java.time.Instant;
import java.util.UUID;

public record PrescriptionSummaryResponse(
        UUID id,
        String prescriptionCode,
        UUID medicalRecordId,
        UUID patientId,
        String patientCode,
        String patientName,
        UUID doctorId,
        String doctorUsername,
        PrescriptionStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
