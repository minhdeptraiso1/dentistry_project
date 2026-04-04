package com.project.base_v1.controller;

import java.time.LocalDate;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.base_v1.dto.request.appointment.SetDoctorShiftCapacityRequest;
import com.project.base_v1.dto.response.appointment.AvailableDoctorResponse;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.enums.WorkShift;
import com.project.base_v1.service.DoctorCapacityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Tag(name = "Doctor Capacity", description = "APIs to configure doctor capacity per day & shift (MORNING/AFTERNOON)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/doctor-capacities")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorCapacityController {

    DoctorCapacityService doctorCapacityService;

    @Operation(
            summary = "Set doctor shift capacity",
            description = """
                        Set max patients for a doctor on a specific date and shift.
                        <br/><b>Roles:</b> ADMIN only
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saved"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ApiResponseSever<Void> setCapacity(@Valid @RequestBody SetDoctorShiftCapacityRequest request) {
        doctorCapacityService.setCapacity(request);
        return ApiResponseSever.ok(null);
    }

    @Operation(
            summary = "Get available doctors",
            description = """
                        Get list of doctors who have capacity set for a specific date and shift.
                        Returns doctor info along with max patients and current patient count.
                        <br/><b>Roles:</b> ADMIN, CASHIER, DOCTOR
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR', 'PATIENT')")
    @GetMapping("/available")
    public ApiResponseSever<List<AvailableDoctorResponse>> getAvailableDoctors(

            @ParameterObject
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate workDate,

            @ParameterObject
            WorkShift shift

    ) {

        List<AvailableDoctorResponse> doctors =
                doctorCapacityService.getAvailableDoctors(workDate, shift);

        return ApiResponseSever.ok(doctors);
    }
}