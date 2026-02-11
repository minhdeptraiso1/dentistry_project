package com.project.base_v1.dto.request.treatment;

import com.project.base_v1.enums.TreatmentPlanStatus;

import java.util.List;

public record UpdateTreatmentPlanRequest(
        String note,
        TreatmentPlanStatus status,          // cho phép đổi status theo rule
        List<CreateTreatmentItemRequest> items // nếu gửi => replace toàn bộ
) {
}
