package com.project.base_v1.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.base_v1.dto.request.appointment.AssignDoctorRequest;
import com.project.base_v1.dto.request.appointment.CreateAppointmentRequest;
import com.project.base_v1.dto.response.appointment.AppointmentResponse;
import com.project.base_v1.enums.WorkShift;

public interface AppointmentService {
    AppointmentResponse create(CreateAppointmentRequest request);

    AppointmentResponse getById(UUID id);

    Page<AppointmentResponse> search(LocalDate date, UUID doctorId, String status, WorkShift shift, Pageable pageable);

    AppointmentResponse assignDoctor(UUID appointmentId, AssignDoctorRequest request);

    void cancel(UUID appointmentId, String note);

    AppointmentResponse start(UUID appointmentId);
    
    AppointmentResponse finish(UUID appointmentId);
}