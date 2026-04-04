package com.project.base_v1.service;

import com.project.base_v1.dto.request.medical_record.CreateMedicalRecordRequest;
import com.project.base_v1.dto.request.medical_record.MedicalRecordSearchRequest;
import com.project.base_v1.dto.request.medical_record.UpdateMedicalRecordRequest;
import com.project.base_v1.dto.response.medical_record.MedicalRecordResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MedicalRecordService {
    MedicalRecordResponse create(CreateMedicalRecordRequest request);

    MedicalRecordResponse getById(UUID id);

    Page<MedicalRecordResponse> search(MedicalRecordSearchRequest request, Pageable pageable);

    MedicalRecordResponse update(UUID id, UpdateMedicalRecordRequest request);

    void delete(UUID id);

    List<MedicalRecordResponse> getMyMedicalRecords();

    MedicalRecordResponse getMyMedicalRecordDetail(UUID id);
}
