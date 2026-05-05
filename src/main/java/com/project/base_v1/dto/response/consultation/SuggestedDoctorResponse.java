package com.project.base_v1.dto.response.consultation;

import java.util.UUID;

public record SuggestedDoctorResponse(
        UUID doctorId,
        String doctorName,
        String description,
        String reason
) {
}