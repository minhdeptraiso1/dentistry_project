package com.project.base_v1.controller;

import com.project.base_v1.dto.request.medical_record.CreateMedicalRecordRequest;
import com.project.base_v1.dto.request.medical_record.MedicalRecordSearchRequest;
import com.project.base_v1.dto.request.medical_record.UpdateMedicalRecordRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.medical_record.MedicalRecordResponse;
import com.project.base_v1.service.MedicalRecordService;
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
import org.springdoc.core.annotations.ParameterObject;
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

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Medical Record",
        description = "APIs for medical records (patient visits)"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/medical-records")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MedicalRecordController {

    MedicalRecordService medicalRecordService;

    @Operation(
            summary = "Create medical record",
            description = """
                    Create a new medical record (visit) for a patient.
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
    public ApiResponseSever<MedicalRecordResponse> create(
            @Valid @RequestBody CreateMedicalRecordRequest request
    ) {
        return ApiResponseSever.ok(medicalRecordService.create(request));
    }

    @Operation(
            summary = "Get medical record by id",
            description = """
                    Get medical record detail.
                    <br/>
                    <b>Roles:</b> ADMIN, DOCTOR, CASHIER
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','CASHIER')")
    @GetMapping("/{id}")
    public ApiResponseSever<MedicalRecordResponse> getById(
            @Parameter(description = "Medical record ID (UUID)")
            @PathVariable UUID id
    ) {
        return ApiResponseSever.ok(medicalRecordService.getById(id));
    }

    @Operation(
            summary = "Search medical records",
            description = """
                    Search medical records by keyword/patient/doctor/date range.
                    <br/>
                    <b>Roles:</b> ADMIN, DOCTOR, CASHIER
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','CASHIER')")
    @GetMapping
    public ApiResponseSever<Page<MedicalRecordResponse>> search(
            @ParameterObject MedicalRecordSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(medicalRecordService.search(request, pageable));
    }

    @Operation(
            summary = "Update medical record",
            description = """
                    Update medical record.
                    <br/>
                    <b>Roles:</b> DOCTOR, ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping("/{id}")
    public ApiResponseSever<MedicalRecordResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateMedicalRecordRequest request
    ) {
        return ApiResponseSever.ok(medicalRecordService.update(id, request));
    }

    @Operation(
            summary = "Delete medical record (soft delete)",
            description = """
                    Soft delete medical record.
                    <br/>
                    <b>Roles:</b> ADMIN only
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponseSever<Void> delete(@PathVariable UUID id) {
        medicalRecordService.delete(id);
        return ApiResponseSever.ok(null);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my")
    public List<MedicalRecordResponse> getMyMedicalRecords() {
        return medicalRecordService.getMyMedicalRecords();
    }

    @Operation(summary = "Get my medical record detail", description = "<b>Roles:</b> PATIENT")
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my/{id}")
    public ApiResponseSever<MedicalRecordResponse> getMyMedicalRecordDetail(@PathVariable UUID id) {
        return ApiResponseSever.ok(medicalRecordService.getMyMedicalRecordDetail(id));
    }
}
