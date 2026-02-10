package com.project.base_v1.dto.request.patient;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreatePatientRequest(
        @NotBlank String fullName,
        String gender,
        String phone,
        LocalDate dob,
        String address,
        String note
) {
}
