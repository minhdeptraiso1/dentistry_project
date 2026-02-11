package com.project.base_v1.service;

import com.project.base_v1.dto.request.treatment.CreateTreatmentPlanRequest;
import com.project.base_v1.dto.request.treatment.UpdateTreatmentPlanRequest;
import com.project.base_v1.dto.response.treatment.TreatmentPlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TreatmentPlanService {
    TreatmentPlanResponse create(CreateTreatmentPlanRequest request);

    TreatmentPlanResponse getById(UUID id);

    Page<TreatmentPlanResponse> listByPatient(UUID patientId, Pageable pageable);

    TreatmentPlanResponse update(UUID id, UpdateTreatmentPlanRequest request);

    void delete(UUID id);

    TreatmentPlanResponse markItemDone(UUID planId, UUID itemId);
}
