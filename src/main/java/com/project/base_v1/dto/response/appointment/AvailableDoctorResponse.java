package com.project.base_v1.dto.response.appointment;

import java.util.UUID;


public record AvailableDoctorResponse(
        UUID doctorId,

        String username,

        String doctorName,

        Integer maxPatients,

        Integer currentPatients
) {
}