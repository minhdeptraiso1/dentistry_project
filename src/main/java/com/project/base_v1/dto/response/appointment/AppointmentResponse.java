package com.project.base_v1.dto.response.appointment;

import com.project.base_v1.enums.AppointmentPriority;
import com.project.base_v1.enums.AppointmentStatus;
import com.project.base_v1.enums.WorkShift;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        String appointmentCode,
        UUID patientId,
        String patientCode,
        String patientName,
        UUID doctorId,
        String doctorUsername,
        UUID treatmentPlanId,
        String treatmentPlanCode,
        LocalDate workDate,
        LocalDate actualDate,
        UUID parentId,
        Integer sequenceNo,
        WorkShift shift,
        AppointmentStatus status,
        AppointmentPriority priority,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}