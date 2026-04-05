package com.project.base_v1.controller;

import com.project.base_v1.dto.request.appointment.CreateDoctorScheduleRequest;
import com.project.base_v1.dto.request.appointment.DoctorScheduleRequestSearchRequest;
import com.project.base_v1.dto.response.appointment.DoctorScheduleRequestResponse;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.service.DoctorScheduleRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Doctor Schedule Request", description = "Doctor đăng ký lịch, admin duyệt")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/doctor-schedule-requests")
@RequiredArgsConstructor
public class DoctorScheduleRequestController {

    private final DoctorScheduleRequestService service;

    @Operation(
            summary = "Doctor create schedule request",
            description = """
                        Doctor đăng ký lịch làm việc theo ngày + ca.
                        <br/><b>Roles:</b> DOCTOR
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponseSever<Void> create(
            @Valid @RequestBody CreateDoctorScheduleRequest request
    ) {
        service.createRequest(request);
        return ApiResponseSever.ok(null);
    }

    @Operation(
            summary = "Approve doctor schedule request",
            description = """
                        Admin duyệt lịch của bác sĩ.
                        Khi duyệt sẽ tạo DoctorShiftCapacity.
                        <br/><b>Roles:</b> ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Approved"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponseSever<Void> approve(@PathVariable UUID id) {
        service.approve(id);
        return ApiResponseSever.ok(null);
    }

    @Operation(
            summary = "Reject doctor schedule request",
            description = """
                        Admin từ chối lịch bác sĩ.
                        <br/><b>Roles:</b> ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rejected"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponseSever<Void> reject(@PathVariable UUID id) {
        service.reject(id);
        return ApiResponseSever.ok(null);
    }

    @Operation(
            summary = "Search doctor schedule requests",
            description = """
                        Lọc request theo ngày, ca, bác sĩ, trạng thái.
                        <br/><b>Roles:</b> ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponseSever<Page<DoctorScheduleRequestResponse>> getAll(
            @ParameterObject DoctorScheduleRequestSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(service.getAll(request, pageable));
    }
}