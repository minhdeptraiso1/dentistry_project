package com.project.base_v1.service;

import com.project.base_v1.dto.response.dashboard.CategoryAmountResponse;
import com.project.base_v1.dto.response.dashboard.DashboardSummaryResponse;
import com.project.base_v1.dto.response.dashboard.TimePointAmountResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse summary(LocalDate from, LocalDate to);

    List<TimePointAmountResponse> revenueByDay(LocalDate from, LocalDate to);

    List<CategoryAmountResponse> revenueByServiceType(LocalDate from, LocalDate to);

    List<TimePointAmountResponse> medicineImportCostByDay(LocalDate from, LocalDate to);

    List<TimePointAmountResponse> medicineDispensedByDay(LocalDate from, LocalDate to);

    List<CategoryAmountResponse> topDispensedMedicines(LocalDate from, LocalDate to);
}
