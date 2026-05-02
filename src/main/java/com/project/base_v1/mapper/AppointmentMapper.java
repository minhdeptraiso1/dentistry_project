package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.appointment.AppointmentResponse;
import com.project.base_v1.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientCode", source = "patient.patientCode")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorUsername", source = "doctor.username")
    @Mapping(target = "treatmentPlanId", source = "treatmentPlan.id")
    @Mapping(target = "treatmentPlanCode", source = "treatmentPlan.planCode")
    AppointmentResponse toResponse(Appointment a);
}