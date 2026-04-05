package com.project.base_v1.service;

import com.project.base_v1.dto.request.appointment.CreateDoctorScheduleRequest;
import com.project.base_v1.dto.request.appointment.DoctorScheduleRequestSearchRequest;
import com.project.base_v1.dto.response.appointment.DoctorScheduleRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DoctorScheduleRequestService {

    void createRequest(CreateDoctorScheduleRequest request);

    void approve(UUID id);

    void reject(UUID id);

    Page<DoctorScheduleRequestResponse> getAll(DoctorScheduleRequestSearchRequest request, Pageable pageable);
}