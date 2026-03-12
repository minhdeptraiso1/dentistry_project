package com.project.base_v1.dto.request.appointment;

import com.project.base_v1.enums.AppointmentPriority;
import com.project.base_v1.enums.WorkShift;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull UUID patientId,
        UUID doctorId,              // optional
        @NotNull LocalDate workDate,
        @NotNull WorkShift shift,
        AppointmentPriority priority,
        String note
) {
}