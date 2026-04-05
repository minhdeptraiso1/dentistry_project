package com.project.base_v1.dto.request.appointment;

import com.project.base_v1.enums.WorkShift;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateDoctorScheduleRequest(

        @NotNull
        LocalDate workDate,

        @NotNull
        WorkShift shift,

        @NotNull
        Integer maxPatients

) {
}