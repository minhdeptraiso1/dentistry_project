package com.project.base_v1.dto.request.service;

import java.math.BigDecimal;
import java.util.List;

public record UpdateServiceRequest(
        String name,
        String category,
        String description,
        BigDecimal basePrice,
        String unit,
        Integer durationMin,
        Boolean active,
        List<CreatePackageStepRequest> steps // update theo kiểu replace toàn bộ steps
) {
}
