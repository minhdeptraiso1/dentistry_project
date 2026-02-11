package com.project.base_v1.dto.response.prescription;

import com.project.base_v1.enums.PrescriptionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PrescriptionResponse(
        UUID id,
        String prescriptionCode,
        UUID medicalRecordId,
        UUID patientId,
        String patientCode,
        String patientName,
        UUID doctorId,
        String doctorUsername,
        PrescriptionStatus status,
        String note,
        List<PrescriptionItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}
