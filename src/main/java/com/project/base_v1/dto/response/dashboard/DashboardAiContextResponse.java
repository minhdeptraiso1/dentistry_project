package com.project.base_v1.dto.response.dashboard;

import com.project.base_v1.dto.response.inventory.BatchExpiryWarningResponse;
import com.project.base_v1.dto.response.inventory.MedicineStockSummaryResponse;

import java.util.List;
import java.util.Map;

public record DashboardAiContextResponse(
        String periodLabel,
        DashboardSummaryResponse currentMonthSummary,
        DashboardSummaryResponse previousMonthSummary,
        DashboardSummaryResponse sameMonthLastYearSummary,
        Map<String, Object> growthMetrics,
        List<TimePointAmountResponse> revenueByDay,
        List<CategoryAmountResponse> revenueByServiceType,
        List<TimePointAmountResponse> medicineImportCostByDay,
        List<TimePointAmountResponse> medicineDispensedByDay,
        List<CategoryAmountResponse> topDispensedMedicines,
        List<MedicineStockSummaryResponse> stockSummary,
        List<BatchExpiryWarningResponse> expiryWarnings,
        List<MedicineProcurementSuggestionResponse> procurementSuggestions
) {
}