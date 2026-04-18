package com.project.base_v1.controller;

import java.util.List;
import java.util.UUID;

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

import com.project.base_v1.dto.request.patient.CreatePatientRequest;
import com.project.base_v1.dto.request.patient.PatientSearchRequest;
import com.project.base_v1.dto.request.patient.UpdatePatientRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.patient.ActiveDoctorResponse;
import com.project.base_v1.dto.response.patient.PatientResponse;
import com.project.base_v1.service.PatientService;
import com.project.base_v1.service.UserService;

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

@Tag(
        name = "Patient",
        description = "APIs for patient management (Nha khoa)"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PatientController {

    PatientService patientService;
        UserService userService;

    // ===================== CREATE =====================
    @Operation(
            summary = "Create new patient",
            description = """
                    Create a new patient.
                    <br/>
                    <b>Roles:</b> ADMIN, CASHIER
                    <br/>
                    <b>Note:</b> patientCode is generated automatically (BN000001...)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Patient created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponseSever<PatientResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Patient creation payload",
                    required = true
            )
            @Valid @RequestBody CreatePatientRequest request
    ) {
        return ApiResponseSever.ok(patientService.create(request));
    }

    // ===================== GET DETAIL =====================
    @Operation(
            summary = "Get patient by id",
            description = """
                    Get patient detail by UUID.
                    <br/>
                    <b>Roles:</b> ADMIN, CASHIER, DOCTOR
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR')")
    @GetMapping("/{id}")
    public ApiResponseSever<PatientResponse> getById(
            @Parameter(
                    description = "Patient ID (UUID)",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id
    ) {
        return ApiResponseSever.ok(patientService.getById(id));
    }

    // ===================== SEARCH =====================
    @Operation(
            summary = "Search patients",
            description = """
                    Search patients by phone and/or keyword.
                    <br/>
                    <b>Keyword</b> matches: fullName OR patientCode (LIKE, case-insensitive).
                    <br/>
                    <b>Phone</b> matches: phone LIKE.
                    <br/>
                    <b>Roles:</b> ADMIN, CASHIER, DOCTOR
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR')")
    @GetMapping
    public ApiResponseSever<Page<PatientResponse>> search(
            @Parameter(
                    description = "Search payload (query params)",
                    examples = {
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Search by name",
                                    description = "Search by patient name",
                                    value = "keyword=nguyen"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Search by phone",
                                    description = "Search by phone contains",
                                    value = "phone=098"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Search by patient code",
                                    description = "Search by patient code",
                                    value = "keyword=BN0000"
                            )
                    }
            )
            @ParameterObject PatientSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(patientService.search(request, pageable));
    }

    // ===================== UPDATE =====================
    @Operation(
            summary = "Update patient",
            description = """
                    Update patient by id.
                    <br/>
                    <b>Roles:</b> ADMIN, CASHIER
                    <br/>
                    <b>Note:</b> patientCode cannot be updated.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient updated"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @PutMapping("/{id}")
    public ApiResponseSever<PatientResponse> update(
            @Parameter(
                    description = "Patient ID (UUID)",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Patient update payload",
                    required = true
            )
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        return ApiResponseSever.ok(patientService.update(id, request));
    }

    // ===================== DELETE (SOFT) =====================
    @Operation(
            summary = "Delete patient (soft delete)",
            description = """
                    Soft delete patient by id.
                    <br/>
                    <b>Roles:</b> ADMIN only
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient deleted"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponseSever<Void> delete(
            @Parameter(
                    description = "Patient ID (UUID)",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id
    ) {
        patientService.delete(id);
        return ApiResponseSever.ok(null);
    }

    @GetMapping("/me")
    public PatientResponse getMyProfile() {
        return patientService.getMyProfile();
    }

        @PreAuthorize("hasRole('PATIENT')")
        @GetMapping("/me/active-doctors")
        public ApiResponseSever<List<ActiveDoctorResponse>> getActiveDoctorsForPatient() {
                return ApiResponseSever.ok(userService.getActiveDoctorsForPatient());
        }
}
