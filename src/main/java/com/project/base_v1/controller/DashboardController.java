package com.project.base_v1.controller;

import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.dashboard.CategoryAmountResponse;
import com.project.base_v1.dto.response.dashboard.DashboardSummaryResponse;
import com.project.base_v1.dto.response.dashboard.TimePointAmountResponse;
import com.project.base_v1.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Dashboard", description = "APIs for revenue/cost/medicine dashboard reports")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {

    DashboardService dashboardService;

    @Operation(
            summary = "Dashboard summary",
            description = """
                        Summary revenue/cost/profit in date range.
                        <br/><b>Roles:</b> ADMIN, CASHIER
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/summary")
    public ApiResponseSever<DashboardSummaryResponse> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponseSever.ok(dashboardService.summary(from, to));
    }

    @Operation(summary = "Revenue by day", description = "<b>Roles:</b> ADMIN, CASHIER")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/revenue-by-day")
    public ApiResponseSever<List<TimePointAmountResponse>> revenueByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponseSever.ok(dashboardService.revenueByDay(from, to));
    }

    @Operation(summary = "Revenue by service type (SERVICE/MEDICINE)", description = "<b>Roles:</b> ADMIN, CASHIER")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/revenue-by-service-type")
    public ApiResponseSever<List<CategoryAmountResponse>> revenueByServiceType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponseSever.ok(dashboardService.revenueByServiceType(from, to));
    }

    @Operation(summary = "Medicine import cost by day", description = "<b>Roles:</b> ADMIN, CASHIER")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/medicine-import-cost-by-day")
    public ApiResponseSever<List<TimePointAmountResponse>> medicineImportCostByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponseSever.ok(dashboardService.medicineImportCostByDay(from, to));
    }

    @Operation(summary = "Medicine dispensed quantity by day", description = "<b>Roles:</b> ADMIN, CASHIER, DOCTOR")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR')")
    @GetMapping("/medicine-dispensed-by-day")
    public ApiResponseSever<List<TimePointAmountResponse>> medicineDispensedByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponseSever.ok(dashboardService.medicineDispensedByDay(from, to));
    }

    @Operation(summary = "Top dispensed medicines", description = "<b>Roles:</b> ADMIN, CASHIER, DOCTOR")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR')")
    @GetMapping("/top-dispensed-medicines")
    public ApiResponseSever<List<CategoryAmountResponse>> topDispensedMedicines(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponseSever.ok(dashboardService.topDispensedMedicines(from, to));
    }
}
