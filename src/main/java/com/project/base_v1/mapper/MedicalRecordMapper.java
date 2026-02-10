package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.medical_record.MedicalRecordResponse;
import com.project.base_v1.entity.MedicalRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientCode", source = "patient.patientCode")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorUsername", source = "doctor.username")
    MedicalRecordResponse toResponse(MedicalRecord entity);
}
