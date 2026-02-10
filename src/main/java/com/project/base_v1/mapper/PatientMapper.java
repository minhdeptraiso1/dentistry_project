package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.patient.PatientResponse;
import com.project.base_v1.entity.Patient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientResponse toResponse(Patient patient);
}
