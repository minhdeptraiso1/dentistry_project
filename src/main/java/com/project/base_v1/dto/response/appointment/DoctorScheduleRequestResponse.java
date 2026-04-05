package com.project.base_v1.dto.response.appointment;

import com.project.base_v1.enums.ScheduleRequestStatus;
import com.project.base_v1.enums.WorkShift;

import java.time.LocalDate;
import java.util.UUID;

public record DoctorScheduleRequestResponse(
        UUID id,
        UUID doctorId,
        String doctorName,
        LocalDate workDate,
        WorkShift shift,
        Integer maxPatients,
        ScheduleRequestStatus status

) {
}