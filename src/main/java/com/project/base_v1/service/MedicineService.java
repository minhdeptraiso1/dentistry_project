package com.project.base_v1.service;

import com.project.base_v1.dto.request.medicine.CreateMedicineRequest;
import com.project.base_v1.dto.request.medicine.ImportBatchRequest;
import com.project.base_v1.dto.response.medicine.MedicineBatchResponse;
import com.project.base_v1.dto.response.medicine.MedicineResponse;

import java.util.UUID;

public interface MedicineService {
    MedicineResponse create(CreateMedicineRequest request);

    MedicineBatchResponse importBatch(ImportBatchRequest request);

    MedicineResponse getById(UUID id);
}
