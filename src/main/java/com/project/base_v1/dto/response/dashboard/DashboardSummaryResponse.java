package com.project.base_v1.dto.response.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal grossRevenue,
        BigDecimal discountAmount,
        BigDecimal netRevenue,
        BigDecimal paidAmount,
        BigDecimal unpaidAmount,

        BigDecimal operatingExpenses,     // expenses table
        BigDecimal medicineImportCost,    // sum(import_price*quantity_in)
        BigDecimal totalCosts,            // operating + import
        BigDecimal estimatedProfit        // netRevenue - totalCosts
) {
}
