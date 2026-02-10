package com.project.base_v1.dto.request.patient;

public record PatientSearchRequest(
        String keyword, // name / code
        String phone
) {
}
