package com.project.base_v1.service;

import com.project.base_v1.dto.response.inventory.BatchExpiryWarningResponse;
import com.project.base_v1.dto.response.inventory.MedicineStockSummaryResponse;

import java.util.List;

public interface InventoryReportService {
    List<MedicineStockSummaryResponse> stockSummary(Integer nearDays, Integer lowStockThreshold, Boolean activeOnly);

    List<BatchExpiryWarningResponse> expiryWarnings(Integer nearDays);
}
