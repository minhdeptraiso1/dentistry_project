package com.project.base_v1.dto.request.medicine;

public record MedicineSearchRequest(
        String keyword,
        Boolean active
) {
}
