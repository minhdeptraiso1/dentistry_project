package com.project.base_v1.controller;

import com.project.base_v1.dto.request.prescription.CreatePrescriptionRequest;
import com.project.base_v1.dto.request.prescription.DispenseRequest;
import com.project.base_v1.dto.request.prescription.PrescriptionSearchRequest;
import com.project.base_v1.dto.request.prescription.UpdatePrescriptionRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.prescription.PrescriptionResponse;
import com.project.base_v1.dto.response.prescription.PrescriptionSummaryResponse;
import com.project.base_v1.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Prescription", description = "APIs for prescriptions & dispensing (deduct stock)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrescriptionController {

    PrescriptionService prescriptionService;

    @Operation(summary = "Create prescription", description = "<b>Roles:</b> DOCTOR, ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created")
    })
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponseSever<PrescriptionResponse> create(@Valid @RequestBody CreatePrescriptionRequest request) {
        return ApiResponseSever.ok(prescriptionService.create(request));
    }

    @Operation(summary = "Get prescription detail", description = "<b>Roles:</b> ADMIN, DOCTOR, CASHIER")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','CASHIER')")
    @GetMapping("/{id}")
    public ApiResponseSever<PrescriptionResponse> getById(@PathVariable UUID id) {
        return ApiResponseSever.ok(prescriptionService.getById(id));
    }

    @Operation(summary = "Update prescription", description = "Replace items if provided. <b>Roles:</b> DOCTOR, ADMIN")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping("/{id}")
    public ApiResponseSever<PrescriptionResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdatePrescriptionRequest request
    ) {
        return ApiResponseSever.ok(prescriptionService.update(id, request));
    }

    @Operation(summary = "Dispense prescription (deduct stock FIFO)", description = "<b>Roles:</b> CASHIER, ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dispensed"),
            @ApiResponse(responseCode = "400", description = "Stock not enough / invalid status")
    })
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN')")
    @PostMapping("/{id}/dispense")
    public ApiResponseSever<PrescriptionResponse> dispense(
            @PathVariable UUID id,
            @RequestBody(required = false) DispenseRequest request
    ) {
        return ApiResponseSever.ok(prescriptionService.dispense(id, request));
    }

    @Operation(summary = "Cancel prescription", description = "<b>Roles:</b> DOCTOR, ADMIN")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/{id}/cancel")
    public ApiResponseSever<Void> cancel(
            @PathVariable UUID id,
            @RequestParam(required = false) String note
    ) {
        prescriptionService.cancel(id, note);
        return ApiResponseSever.ok(null);
    }

    @Operation(summary = "Search prescriptions", description = """
            Filter by keyword, patientId, doctorId, status, fromDate, toDate.
            <br/><b>Roles:</b> ADMIN, DOCTOR, CASHIER
            """)
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','CASHIER')")
    @GetMapping
    public ApiResponseSever<Page<PrescriptionSummaryResponse>> search(
            @ParameterObject PrescriptionSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(prescriptionService.search(request, pageable));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my")
    public List<PrescriptionResponse> getMyPrescriptions() {
        return prescriptionService.getMyPrescriptions();
    }

    @Operation(summary = "Get my prescription detail", description = "<b>Roles:</b> PATIENT")
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my/{id}")
    public ApiResponseSever<PrescriptionResponse> getMyPrescriptionDetail(@PathVariable UUID id) {
        return ApiResponseSever.ok(prescriptionService.getMyPrescriptionDetail(id));
    }
}
