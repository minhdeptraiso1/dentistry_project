package com.project.base_v1.service;

import com.project.base_v1.dto.request.medicine.CreateMedicineRequest;
import com.project.base_v1.dto.request.medicine.ImportBatchRequest;
import com.project.base_v1.dto.request.medicine.MedicineSearchRequest;
import com.project.base_v1.dto.request.medicine.SetMedicinePriceRequest;
import com.project.base_v1.dto.response.medicine.MedicineBatchResponse;
import com.project.base_v1.dto.response.medicine.MedicinePriceHistoryResponse;
import com.project.base_v1.dto.response.medicine.MedicineResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MedicineService {
    MedicineResponse create(CreateMedicineRequest request);

    MedicineBatchResponse importBatch(ImportBatchRequest request);

    MedicineResponse getById(UUID id);

    MedicineResponse setSalePrice(UUID medicineId, SetMedicinePriceRequest request);

    Page<MedicinePriceHistoryResponse> priceHistory(UUID medicineId, Pageable pageable);

    Page<MedicineResponse> search(MedicineSearchRequest request, Pageable pageable);

    Page<MedicineBatchResponse> batchHistory(UUID medicineId, Pageable pageable);

    void disposeBatch(UUID batchId, String reason);
}
