package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.prescription.PrescriptionItemResponse;
import com.project.base_v1.dto.response.prescription.PrescriptionResponse;
import com.project.base_v1.dto.response.prescription.PrescriptionSummaryResponse;
import com.project.base_v1.entity.Prescription;
import com.project.base_v1.entity.PrescriptionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {

    @Mapping(target = "medicalRecordId", source = "medicalRecord.id")
    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientCode", source = "patient.patientCode")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorUsername", source = "doctor.username")
    PrescriptionResponse toResponse(Prescription entity);

    @Mapping(target = "medicineId", source = "medicine.id")
    PrescriptionItemResponse toItemResponse(PrescriptionItem item);

    @Mapping(target = "medicalRecordId", source = "medicalRecord.id")
    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientCode", source = "patient.patientCode")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorUsername", source = "doctor.username")
    PrescriptionSummaryResponse toSummary(Prescription entity);
}
