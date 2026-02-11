package com.project.base_v1.service.impl;

import com.project.base_v1.dto.response.inventory.BatchExpiryWarningResponse;
import com.project.base_v1.dto.response.inventory.MedicineStockSummaryResponse;
import com.project.base_v1.entity.MedicineBatch;
import com.project.base_v1.repository.InventoryReportRepository;
import com.project.base_v1.repository.MedicineBatchRepository;
import com.project.base_v1.repository.projection.MedicineStockSummaryProjection;
import com.project.base_v1.service.InventoryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryReportServiceImpl implements InventoryReportService {

    private final InventoryReportRepository reportRepo;
    private final MedicineBatchRepository batchRepo;

    @Override
    public List<MedicineStockSummaryResponse> stockSummary(Integer nearDays, Integer lowStockThreshold, Boolean activeOnly) {
        int nd = (nearDays == null || nearDays <= 0) ? 30 : nearDays;
        int threshold = (lowStockThreshold == null || lowStockThreshold < 0) ? 10 : lowStockThreshold;
        boolean onlyActive = activeOnly != null && activeOnly;

        LocalDate today = LocalDate.now();
        LocalDate nearDate = today.plusDays(nd);

        List<MedicineStockSummaryProjection> rows = reportRepo.getStockSummary(today, nearDate, onlyActive);
        List<MedicineStockSummaryResponse> res = new ArrayList<>();

        for (MedicineStockSummaryProjection r : rows) {
            boolean low = r.getTotalRemaining() != null && r.getTotalRemaining() <= threshold;
            boolean hasExpired = r.getExpiredBatchesCount() != null && r.getExpiredBatchesCount() > 0;
            boolean hasNear = r.getNearExpiryBatchesCount() != null && r.getNearExpiryBatchesCount() > 0;

            res.add(new MedicineStockSummaryResponse(
                    r.getMedicineId(),
                    r.getMedicineCode(),
                    r.getMedicineName(),
                    r.getUnit(),
                    r.getTotalRemaining(),
                    r.getTotalBatches(),
                    r.getNearestExpiryDate(),
                    r.getExpiredBatchesCount(),
                    r.getNearExpiryBatchesCount(),
                    low,
                    hasExpired,
                    hasNear
            ));
        }
        return res;
    }

    @Override
    public List<BatchExpiryWarningResponse> expiryWarnings(Integer nearDays) {
        int nd = (nearDays == null || nearDays <= 0) ? 30 : nearDays;
        LocalDate today = LocalDate.now();
        LocalDate nearDate = today.plusDays(nd);

        List<MedicineBatch> batches = batchRepo.findBatchesExpiredOrNear(today, nearDate);
        List<BatchExpiryWarningResponse> res = new ArrayList<>();

        for (MedicineBatch b : batches) {
            String type = (b.getExpiryDate() != null && b.getExpiryDate().isBefore(today))
                    ? "EXPIRED"
                    : "NEAR_EXPIRY";

            res.add(new BatchExpiryWarningResponse(
                    b.getId(),
                    b.getMedicine().getId(),
                    b.getMedicine().getCode(),
                    b.getMedicine().getName(),
                    b.getBatchNo(),
                    b.getImportDate(),
                    b.getExpiryDate(),
                    b.getQuantityRemaining(),
                    type
            ));
        }
        return res;
    }
}
