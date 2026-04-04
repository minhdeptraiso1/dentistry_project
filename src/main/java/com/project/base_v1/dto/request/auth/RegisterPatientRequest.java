package com.project.base_v1.dto.request.auth;

public record RegisterPatientRequest(
        String username,
        String password,
        String patientCode,
        String email

) {
}
