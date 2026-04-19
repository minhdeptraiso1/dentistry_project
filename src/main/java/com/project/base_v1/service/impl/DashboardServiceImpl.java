package com.project.base_v1.service.impl;

import com.project.base_v1.dto.response.dashboard.CategoryAmountResponse;
import com.project.base_v1.dto.response.dashboard.DashboardSummaryResponse;
import com.project.base_v1.dto.response.dashboard.TimePointAmountResponse;
import com.project.base_v1.repository.DashboardCostRepository;
import com.project.base_v1.repository.DashboardMedicineRepository;
import com.project.base_v1.repository.DashboardRevenueRepository;
import com.project.base_v1.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRevenueRepository revenueRepo;
    private final DashboardCostRepository costRepo;
    private final DashboardMedicineRepository medicineRepo;

    @Override
    public DashboardSummaryResponse summary(LocalDate from, LocalDate to) {

        // from inclusive 00:00, to inclusive -> convert to [from, to+1day)
        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        var r = revenueRepo.revenueSummary(fromTs, toTs);

        BigDecimal operating = costRepo.operatingExpense(from, to);
        BigDecimal importCost = costRepo.medicineImportCost(from, to);

        BigDecimal totalCosts = operating.add(importCost);
        BigDecimal estimatedProfit = r.getNetRevenue().subtract(totalCosts);

        return new DashboardSummaryResponse(
                r.getGrossRevenue(),
                r.getDiscountAmount(),
                r.getNetRevenue(),
                r.getPaidAmount(),
                r.getUnpaidAmount(),
                operating,
                importCost,
                totalCosts,
                estimatedProfit
        );
    }

    @Override
    public List<TimePointAmountResponse> revenueByDay(LocalDate from, LocalDate to) {
        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Object[]> rows = revenueRepo.revenueByDay(fromTs, toTs);
        return mapDateAmount(rows);
    }

    @Override
    public List<CategoryAmountResponse> revenueByServiceType(LocalDate from, LocalDate to) {
        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Object[]> rows = revenueRepo.revenueByServiceType(fromTs, toTs);
        return mapCategoryAmount(rows);
    }

    @Override
    public List<TimePointAmountResponse> medicineImportCostByDay(LocalDate from, LocalDate to) {
        List<Object[]> rows = costRepo.importCostByDay(from, to);
        return mapDateAmount(rows);
    }

    @Override
    public List<TimePointAmountResponse> medicineDispensedByDay(LocalDate from, LocalDate to) {
        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Object[]> rows = medicineRepo.medicineDispensedByDay(fromTs, toTs);
        return mapDateAmount(rows);
    }

    @Override
    public List<CategoryAmountResponse> topDispensedMedicines(LocalDate from, LocalDate to) {
        Instant fromTs = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toTs = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Object[]> rows = medicineRepo.topDispensedMedicines(fromTs, toTs);
        return mapCategoryAmount(rows);
    }

    // ===== helpers =====
    private List<TimePointAmountResponse> mapDateAmount(List<Object[]> rows) {
        List<TimePointAmountResponse> res = new ArrayList<>();

        for (Object[] r : rows) {
            String date = String.valueOf(r[0]);

            BigDecimal amount = BigDecimal.ZERO;
            Object v = r[1];

            if (v != null) {
                if (v instanceof BigDecimal bd) {
                    amount = bd;
                } else if (v instanceof Number n) {
                    amount = BigDecimal.valueOf(n.doubleValue());
                } else {
                    amount = new BigDecimal(v.toString());
                }
            }

            res.add(new TimePointAmountResponse(date, amount));
        }

        return res;
    }

    private List<CategoryAmountResponse> mapCategoryAmount(List<Object[]> rows) {
        List<CategoryAmountResponse> res = new ArrayList<>();

        for (Object[] r : rows) {
            String cat = String.valueOf(r[0]);

            BigDecimal amount = BigDecimal.ZERO;
            Object v = r[1];

            if (v != null) {
                if (v instanceof BigDecimal bd) {
                    amount = bd;
                } else if (v instanceof Number n) {
                    amount = BigDecimal.valueOf(n.doubleValue());
                } else {
                    amount = new BigDecimal(v.toString());
                }
            }

            res.add(new CategoryAmountResponse(cat, amount));
        }

        return res;
    }
}
