package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.treatment.TreatmentItemResponse;
import com.project.base_v1.dto.response.treatment.TreatmentPlanResponse;
import com.project.base_v1.entity.TreatmentItem;
import com.project.base_v1.entity.TreatmentPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TreatmentPlanMapper {

    @Mapping(target = "medicalRecordId", source = "medicalRecord.id")
    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientCode", source = "patient.patientCode")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorUsername", source = "doctor.username")
    TreatmentPlanResponse toResponse(TreatmentPlan plan);

    @Mapping(target = "serviceId", source = "service.id")
    TreatmentItemResponse toItemResponse(TreatmentItem item);
}
