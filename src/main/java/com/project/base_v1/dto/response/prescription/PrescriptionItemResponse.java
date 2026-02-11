package com.project.base_v1.dto.response.prescription;

import java.util.UUID;

public record PrescriptionItemResponse(
        UUID id,
        UUID medicineId,
        String medicineCode,
        String medicineName,
        String unit,
        String dosage,
        Integer quantity,
        String note
) {
}
