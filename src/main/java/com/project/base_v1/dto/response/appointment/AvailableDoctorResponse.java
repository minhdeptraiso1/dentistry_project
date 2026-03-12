package com.project.base_v1.dto.response.appointment;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailableDoctorResponse {

    private UUID doctorId;

    private String username;

    private String doctorName;

    private Integer maxPatients;

    private Integer currentPatients;

}