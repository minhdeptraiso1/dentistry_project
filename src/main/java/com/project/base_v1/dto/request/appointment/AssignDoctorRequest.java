package com.project.base_v1.dto.request.appointment;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignDoctorRequest(
        @NotNull UUID doctorId
) {
}