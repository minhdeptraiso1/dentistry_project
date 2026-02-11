package com.project.base_v1.dto.request.prescription;

import com.project.base_v1.enums.PrescriptionStatus;

import java.util.List;

public record UpdatePrescriptionRequest(
        String note,
        PrescriptionStatus status,
        List<CreatePrescriptionItemRequest> items // replace nếu gửi
) {
}
