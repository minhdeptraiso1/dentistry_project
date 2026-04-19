package com.project.base_v1.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.base_v1.dto.response.dashboard.CategoryAmountResponse;
import com.project.base_v1.dto.response.dashboard.DashboardAiContextResponse;
import com.project.base_v1.dto.response.dashboard.DashboardSummaryResponse;
import com.project.base_v1.dto.response.dashboard.MedicineProcurementSuggestionResponse;
import com.project.base_v1.dto.response.dashboard.TimePointAmountResponse;
import com.project.base_v1.dto.response.inventory.BatchExpiryWarningResponse;
import com.project.base_v1.dto.response.inventory.MedicineStockSummaryResponse;
import com.project.base_v1.repository.DashboardMedicineRepository;
import com.project.base_v1.repository.MedicineBatchRepository;
import com.project.base_v1.repository.projection.MedicineDispensedSummaryProjection;
import com.project.base_v1.service.DashboardAiContextService;
import com.project.base_v1.service.DashboardService;
import com.project.base_v1.service.InventoryReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardAiContextServiceImpl implements DashboardAiContextService {

    private final DashboardService dashboardService;
    private final InventoryReportService inventoryReportService;
    private final DashboardMedicineRepository dashboardMedicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;

    @Override
    public DashboardAiContextResponse buildMonthlyContext(int year, int month) {
        YearMonth currentYm = YearMonth.of(year, month);
        YearMonth prevYm = currentYm.minusMonths(1);
        YearMonth sameYmLastYear = currentYm.minusYears(1);

        LocalDate currentFrom = currentYm.atDay(1);
        LocalDate currentTo = currentYm.atEndOfMonth();

        LocalDate prevFrom = prevYm.atDay(1);
        LocalDate prevTo = prevYm.atEndOfMonth();

        LocalDate lastYearFrom = sameYmLastYear.atDay(1);
        LocalDate lastYearTo = sameYmLastYear.atEndOfMonth();

        DashboardSummaryResponse currentSummary = dashboardService.summary(currentFrom, currentTo);
        DashboardSummaryResponse previousSummary = dashboardService.summary(prevFrom, prevTo);
        DashboardSummaryResponse sameMonthLastYearSummary = dashboardService.summary(lastYearFrom, lastYearTo);

        List<TimePointAmountResponse> revenueByDay = dashboardService.revenueByDay(currentFrom, currentTo);
        List<CategoryAmountResponse> revenueByServiceType = dashboardService.revenueByServiceType(currentFrom, currentTo);
        List<TimePointAmountResponse> medicineImportCostByDay = dashboardService.medicineImportCostByDay(currentFrom, currentTo);
        List<TimePointAmountResponse> medicineDispensedByDay = dashboardService.medicineDispensedByDay(currentFrom, currentTo);
        List<CategoryAmountResponse> topDispensedMedicines = dashboardService.topDispensedMedicines(currentFrom, currentTo);

        List<MedicineStockSummaryResponse> stockSummary =
                inventoryReportService.stockSummary(30, 10, false);

        List<BatchExpiryWarningResponse> expiryWarnings =
                inventoryReportService.expiryWarnings(30);

        Map<String, Object> growthMetrics = buildGrowthMetrics(
                currentSummary,
                previousSummary,
                sameMonthLastYearSummary
        );

        List<MedicineProcurementSuggestionResponse> procurementSuggestions =
                buildProcurementSuggestions(currentFrom, currentTo, stockSummary, expiryWarnings);

        return new DashboardAiContextResponse(
                "Tháng %02d/%d".formatted(month, year),
                currentSummary,
                previousSummary,
                sameMonthLastYearSummary,
                growthMetrics,
                revenueByDay,
                revenueByServiceType,
                medicineImportCostByDay,
                medicineDispensedByDay,
                topDispensedMedicines,
                stockSummary,
                expiryWarnings,
                procurementSuggestions
        );
    }

    private Map<String, Object> buildGrowthMetrics(
            DashboardSummaryResponse current,
            DashboardSummaryResponse prev,
            DashboardSummaryResponse lastYear
    ) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("netRevenueVsPrevMonthPct",
                growthPercent(current.netRevenue(), prev.netRevenue()));
        map.put("estimatedProfitVsPrevMonthPct",
                growthPercent(current.estimatedProfit(), prev.estimatedProfit()));
        map.put("medicineImportCostVsPrevMonthPct",
                growthPercent(current.medicineImportCost(), prev.medicineImportCost()));

        map.put("netRevenueVsSameMonthLastYearPct",
                growthPercent(current.netRevenue(), lastYear.netRevenue()));
        map.put("estimatedProfitVsSameMonthLastYearPct",
                growthPercent(current.estimatedProfit(), lastYear.estimatedProfit()));
        map.put("medicineImportCostVsSameMonthLastYearPct",
                growthPercent(current.medicineImportCost(), lastYear.medicineImportCost()));

        return map;
    }

    private BigDecimal growthPercent(BigDecimal current, BigDecimal base) {
        if (current == null) current = BigDecimal.ZERO;
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return current.subtract(base)
                .multiply(BigDecimal.valueOf(100))
                .divide(base, 2, RoundingMode.HALF_UP);
    }

    private List<MedicineProcurementSuggestionResponse> buildProcurementSuggestions(
            LocalDate from,
            LocalDate to,
            List<MedicineStockSummaryResponse> stockSummary,
            List<BatchExpiryWarningResponse> expiryWarnings
    ) {
        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<MedicineDispensedSummaryProjection> usageRows =
                dashboardMedicineRepository.dispensedByMedicine(fromTs, toTs);

        Map<UUID, MedicineDispensedSummaryProjection> usageMap = usageRows.stream()
                .collect(Collectors.toMap(MedicineDispensedSummaryProjection::getMedicineId, Function.identity()));

        Map<UUID, Integer> nearExpiryQtyMap = expiryWarnings.stream()
                .filter(w -> "NEAR_EXPIRY".equalsIgnoreCase(w.warningType()) || "EXPIRED".equalsIgnoreCase(w.warningType()))
                .collect(Collectors.groupingBy(
                        BatchExpiryWarningResponse::medicineId,
                        Collectors.summingInt(w -> Optional.ofNullable(w.quantityRemaining()).orElse(0))
                ));

        int daysInMonth = Math.max(1, to.getDayOfMonth());

        List<MedicineProcurementSuggestionResponse> result = new ArrayList<>();

        for (MedicineStockSummaryResponse stock : stockSummary) {
            MedicineDispensedSummaryProjection usage = usageMap.get(stock.medicineId());
            int monthlyDispensedQty = usage != null && usage.getDispensedQty() != null ? usage.getDispensedQty() : 0;
            int currentStock = Optional.ofNullable(stock.totalRemaining()).orElse(0);
            int nearExpiryQty = nearExpiryQtyMap.getOrDefault(stock.medicineId(), 0);

            double avgDaily = (double) monthlyDispensedQty / daysInMonth;
            int usableStock = Math.max(currentStock - nearExpiryQty, 0);
            int targetCoverDays = 21;
            int suggestedQty = (int) Math.max(0, Math.ceil(avgDaily * targetCoverDays - usableStock)) ;

            String priority = resolvePriority(stock.lowStock(), nearExpiryQty, monthlyDispensedQty, suggestedQty);
            String reason = buildReason(stock.lowStock(), stock.hasNearExpiry(), stock.hasExpired(),
                    monthlyDispensedQty, currentStock, nearExpiryQty, suggestedQty);

            if (monthlyDispensedQty > 0 || stock.lowStock() || stock.hasNearExpiry() || stock.hasExpired()) {
                result.add(new MedicineProcurementSuggestionResponse(
                        stock.medicineId(),
                        stock.medicineCode(),
                        stock.medicineName(),
                        currentStock,
                        nearExpiryQty,
                        monthlyDispensedQty,
                        suggestedQty,
                        priority,
                        reason
                ));
            }
        }

        result.sort(Comparator
                .comparing((MedicineProcurementSuggestionResponse r) -> priorityScore(r.priority()))
                .thenComparing(MedicineProcurementSuggestionResponse::suggestedQuantity, Comparator.reverseOrder()));

        return result.stream().limit(15).toList();
    }

    private String resolvePriority(boolean lowStock, int nearExpiryQty, int monthlyDispensedQty, int suggestedQty) {
        if ((lowStock && monthlyDispensedQty > 0) || suggestedQty >= 100) {
            return "HIGH";
        }
        if (suggestedQty > 0 || nearExpiryQty > 0 || monthlyDispensedQty > 30) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private int priorityScore(String priority) {
        return switch (priority) {
            case "HIGH" -> 0;
            case "MEDIUM" -> 1;
            default -> 2;
        };
    }

    private String buildReason(
            boolean lowStock,
            boolean hasNearExpiry,
            boolean hasExpired,
            int monthlyDispensedQty,
            int currentStock,
            int nearExpiryQty,
            int suggestedQty
    ) {
        List<String> parts = new ArrayList<>();

        if (lowStock) parts.add("tồn kho thấp");
        if (hasExpired) parts.add("có lô đã hết hạn");
        if (hasNearExpiry) parts.add("có lô gần hết hạn");
        if (monthlyDispensedQty > 0) parts.add("sử dụng trong tháng: " + monthlyDispensedQty);
        parts.add("tồn hiện tại: " + currentStock);
        if (nearExpiryQty > 0) parts.add("số lượng gần/hết hạn: " + nearExpiryQty);
        if (suggestedQty > 0) parts.add("đề xuất nhập thêm: " + suggestedQty);

        return String.join(", ", parts);
    }
}