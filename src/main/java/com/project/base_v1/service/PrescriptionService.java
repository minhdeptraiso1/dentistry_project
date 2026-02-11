package com.project.base_v1.service;

import com.project.base_v1.dto.request.prescription.CreatePrescriptionRequest;
import com.project.base_v1.dto.request.prescription.DispenseRequest;
import com.project.base_v1.dto.request.prescription.UpdatePrescriptionRequest;
import com.project.base_v1.dto.response.prescription.PrescriptionResponse;

import java.util.UUID;

public interface PrescriptionService {
    PrescriptionResponse create(CreatePrescriptionRequest request);

    PrescriptionResponse getById(UUID id);

    PrescriptionResponse update(UUID id, UpdatePrescriptionRequest request);

    PrescriptionResponse dispense(UUID id, DispenseRequest request);

    void cancel(UUID id, String note);
}
