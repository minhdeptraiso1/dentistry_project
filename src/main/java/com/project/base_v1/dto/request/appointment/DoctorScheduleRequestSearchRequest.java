package com.project.base_v1.dto.request.appointment;

import com.project.base_v1.enums.ScheduleRequestStatus;
import com.project.base_v1.enums.WorkShift;

import java.time.LocalDate;
import java.util.UUID;

public record DoctorScheduleRequestSearchRequest(
        LocalDate date,
        WorkShift shift,
        UUID doctorId,
        ScheduleRequestStatus status
) {
}