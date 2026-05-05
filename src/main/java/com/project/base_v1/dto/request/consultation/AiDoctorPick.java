package com.project.base_v1.dto.request.consultation;

import java.util.UUID;

public record AiDoctorPick(
        UUID doctorId,
        String reason
) {
}