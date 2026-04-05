package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.appointment.DoctorScheduleRequestResponse;
import com.project.base_v1.entity.DoctorScheduleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorScheduleRequestMapper {

    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorName", source = "doctor.username")
    DoctorScheduleRequestResponse toResponse(DoctorScheduleRequest entity);
}