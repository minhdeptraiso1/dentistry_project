package com.project.base_v1.dto.response.patient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String patientCode,
        String fullName,
        String gender,
        String phone,
        LocalDate dob,
        String address,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}
