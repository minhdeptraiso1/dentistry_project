package com.project.base_v1.service;

import com.project.base_v1.dto.request.appointment.AssignDoctorRequest;
import com.project.base_v1.dto.request.appointment.CreateAppointmentRequest;
import com.project.base_v1.dto.response.appointment.AppointmentResponse;
import com.project.base_v1.enums.WorkShift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface AppointmentService {
    AppointmentResponse create(CreateAppointmentRequest request);

    AppointmentResponse getById(UUID id);

    Page<AppointmentResponse> search(LocalDate date, UUID doctorId, String status, WorkShift shift, Pageable pageable);

    AppointmentResponse assignDoctor(UUID appointmentId, AssignDoctorRequest request);

    void cancel(UUID appointmentId, String note);

    AppointmentResponse start(UUID appointmentId);

    AppointmentResponse finish(UUID appointmentId);

    Page<AppointmentResponse> getMyAppointments(LocalDate date, Pageable pageable);

    AppointmentResponse getMyAppointmentDetail(UUID id);

    AppointmentResponse createMyAppointment(CreateAppointmentRequest request);
}