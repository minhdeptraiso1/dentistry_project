package com.project.base_v1.controller;

import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.dashboard.DashboardAiContextResponse;
import com.project.base_v1.dto.response.dashboard.DashboardAiInsightResponse;
import com.project.base_v1.service.DashboardAiContextService;
import com.project.base_v1.service.DashboardAiService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Dashboard AI", description = "AI insight APIs for business analysis")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/dashboard/ai")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardAiController {

    DashboardAiService dashboardAiService;
    DashboardAiContextService dashboardAiContextService;

    @Operation(summary = "AI monthly insight", description = "Roles: ADMIN, CASHIER")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/insight")
    public ApiResponseSever<DashboardAiInsightResponse> insight(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ApiResponseSever.ok(dashboardAiService.generateMonthlyInsight(year, month));
    }

    @Operation(summary = "AI monthly context", description = "Roles: ADMIN, CASHIER")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/context")
    public ApiResponseSever<DashboardAiContextResponse> context(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ApiResponseSever.ok(dashboardAiContextService.buildMonthlyContext(year, month));
    }
}