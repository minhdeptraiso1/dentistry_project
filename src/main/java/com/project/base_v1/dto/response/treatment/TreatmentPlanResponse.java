package com.project.base_v1.dto.response.treatment;

import com.project.base_v1.enums.TreatmentPlanStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TreatmentPlanResponse(
        UUID id,
        String planCode,

        UUID medicalRecordId,
        UUID patientId,
        String patientCode,
        String patientName,

        UUID doctorId,
        String doctorUsername,

        TreatmentPlanStatus status,
        String note,

        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount,

        List<TreatmentItemResponse> items,

        Instant createdAt,
        Instant updatedAt
) {
}
