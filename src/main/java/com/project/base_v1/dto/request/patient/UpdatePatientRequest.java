package com.project.base_v1.dto.request.patient;

import java.time.LocalDate;

public record UpdatePatientRequest(
        String fullName,
        String gender,
        String phone,
        LocalDate dob,
        String address,
        String note
) {
}
