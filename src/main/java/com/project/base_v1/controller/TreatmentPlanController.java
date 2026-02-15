package com.project.base_v1.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.project.base_v1.dto.request.treatment.CreateTreatmentPlanRequest;
import com.project.base_v1.dto.request.treatment.UpdateTreatmentPlanRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.treatment.TreatmentPlanResponse;
import com.project.base_v1.service.TreatmentPlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Tag(name = "Treatment Plan", description = "APIs for treatment plans & items")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/treatment-plans")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TreatmentPlanController {

    TreatmentPlanService service;

    @Operation(
            summary = "Create treatment plan",
            description = """
                    Create a treatment plan from a medical record.
                    <br/>
                    planCode is generated automatically (KH000001...).
                    <br/>
                    <b>Roles:</b> DOCTOR, ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponseSever<TreatmentPlanResponse> create(@Valid @RequestBody CreateTreatmentPlanRequest request) {
        return ApiResponseSever.ok(service.create(request));
    }

    @Operation(summary = "Get treatment plan detail", description = "Return plan with items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','CASHIER')")
    @GetMapping("/{id}")
    public ApiResponseSever<TreatmentPlanResponse> getById(@PathVariable UUID id) {
        return ApiResponseSever.ok(service.getById(id));
    }

    @Operation(summary = "List plans by patient", description = "Paging list by patientId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success")
    })
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','CASHIER')")
    @GetMapping("/by-patient/{patientId}")
    public ApiResponseSever<Page<TreatmentPlanResponse>> listByPatient(
            @PathVariable UUID patientId,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return ApiResponseSever.ok(service.listByPatient(patientId, pageable));
    }

    @Operation(summary = "Update treatment plan", description = "Update note/status, replace items if provided")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping("/{id}")
    public ApiResponseSever<TreatmentPlanResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateTreatmentPlanRequest request
    ) {
        return ApiResponseSever.ok(service.update(id, request));
    }

    @Operation(summary = "Delete treatment plan (soft delete)", description = "ADMIN only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponseSever<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponseSever.ok(null);
    }

    @Operation(summary = "Mark treatment item DONE", description = "Set item status DONE; auto update plan status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/{planId}/items/{itemId}/done")
    public ApiResponseSever<TreatmentPlanResponse> markDone(
            @PathVariable UUID planId,
            @PathVariable UUID itemId
    ) {
        return ApiResponseSever.ok(service.markItemDone(planId, itemId));
    }
}
