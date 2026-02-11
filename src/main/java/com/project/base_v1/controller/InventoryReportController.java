package com.project.base_v1.controller;

import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.inventory.BatchExpiryWarningResponse;
import com.project.base_v1.dto.response.inventory.MedicineStockSummaryResponse;
import com.project.base_v1.service.InventoryReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Inventory Report", description = "APIs for stock summary & expiry warnings")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/inventory-reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryReportController {

    InventoryReportService service;

    @Operation(
            summary = "Stock summary by medicine",
            description = """
                        Return stock summary for each medicine.
                        <br/>Params:
                        <ul>
                          <li><b>nearDays</b>: default 30</li>
                          <li><b>lowStockThreshold</b>: default 10</li>
                          <li><b>activeOnly</b>: default false</li>
                        </ul>
                        <br/><b>Roles:</b> ADMIN, CASHIER
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/stock-summary")
    public ApiResponseSever<List<MedicineStockSummaryResponse>> stockSummary(
            @RequestParam(required = false) Integer nearDays,
            @RequestParam(required = false) Integer lowStockThreshold,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly
    ) {
        return ApiResponseSever.ok(service.stockSummary(nearDays, lowStockThreshold, activeOnly));
    }

    @Operation(
            summary = "Expiry warnings",
            description = """
                        List all batches that are expired or near expiry.
                        <br/><b>nearDays</b>: default 30
                        <br/><b>Roles:</b> ADMIN, CASHIER
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/expiry-warnings")
    public ApiResponseSever<List<BatchExpiryWarningResponse>> expiryWarnings(
            @RequestParam(required = false) Integer nearDays
    ) {
        return ApiResponseSever.ok(service.expiryWarnings(nearDays));
    }
}
