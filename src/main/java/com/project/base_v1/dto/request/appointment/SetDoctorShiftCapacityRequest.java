package com.project.base_v1.dto.request.appointment;

import com.project.base_v1.enums.WorkShift;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record SetDoctorShiftCapacityRequest(
        @NotNull UUID doctorId,
        @NotNull LocalDate workDate,
        @NotNull WorkShift shift,
        @NotNull Integer maxPatients
) {
}