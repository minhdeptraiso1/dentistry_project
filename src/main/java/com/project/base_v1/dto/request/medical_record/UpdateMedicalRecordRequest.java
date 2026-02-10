package com.project.base_v1.dto.request.medical_record;

import java.time.Instant;
import java.util.UUID;

public record UpdateMedicalRecordRequest(
        UUID doctorId,     // cho phép đổi bác sĩ (nếu muốn)
        Instant visitDate,
        String symptom,
        String diagnosis,
        String note
) {
}
