package com.project.base_v1.dto.response.patient;

import java.util.UUID;

public record ActiveDoctorResponse(
        UUID id,
        String name,
        String img
) {
}
