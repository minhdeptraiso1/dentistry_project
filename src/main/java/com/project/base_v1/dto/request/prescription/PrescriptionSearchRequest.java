package com.project.base_v1.dto.request.prescription;

import com.project.base_v1.enums.PrescriptionStatus;

import java.time.Instant;
import java.util.UUID;

public record PrescriptionSearchRequest(
        String keyword,
        UUID patientId,
        UUID doctorId,
        PrescriptionStatus status,

        Instant fromDate,

        Instant toDate
) {
}
