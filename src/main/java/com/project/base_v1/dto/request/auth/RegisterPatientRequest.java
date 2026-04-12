package com.project.base_v1.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterPatientRequest(
        @NotBlank String username,
        @NotBlank String password,
        String patientCode,
        @NotBlank @Email String email,

        String fullName,
        String gender,
        String phone,
        java.time.LocalDate dob,
        String address
) {
}