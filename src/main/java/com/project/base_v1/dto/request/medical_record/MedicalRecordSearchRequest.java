package com.project.base_v1.dto.request.medical_record;

import java.time.Instant;

public record MedicalRecordSearchRequest(
        String keyword,
        String patientId,
        String doctorId,
        Instant fromDate,
        Instant toDate
) {
}
