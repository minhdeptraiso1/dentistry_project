package com.project.base_v1.service;

import com.project.base_v1.dto.request.patient.CreatePatientRequest;
import com.project.base_v1.dto.request.patient.PatientSearchRequest;
import com.project.base_v1.dto.request.patient.UpdatePatientRequest;
import com.project.base_v1.dto.response.patient.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PatientService {
    PatientResponse create(CreatePatientRequest request);

    PatientResponse getById(UUID id);

    Page<PatientResponse> search(PatientSearchRequest request, Pageable pageable);

    PatientResponse update(UUID id, UpdatePatientRequest request);

    void delete(UUID id);

    PatientResponse getMyProfile();
}
