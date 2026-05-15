package com.project.base_v1.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.project.base_v1.dto.request.appointment.AssignDoctorRequest;
import com.project.base_v1.dto.request.appointment.CreateAppointmentRequest;
import com.project.base_v1.dto.request.appointment.CreateFollowUpAppointmentRequest;
import com.project.base_v1.dto.response.appointment.AppointmentResponse;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.enums.WorkShift;
import com.project.base_v1.service.AppointmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Tag(name = "Appointment", description = "APIs for appointment scheduling (day + MORNING/AFTERNOON)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentController {

    AppointmentService appointmentService;

    @Operation(
            summary = "Create appointment (visit ticket)",
            description = """
                        Create an appointment with date & shift.
                        <br/>You can optionally pass doctorId to assign immediately.
                        <br/><b>Roles:</b> CASHIER, ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponseSever<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        return ApiResponseSever.ok(appointmentService.create(request));
    }

    @Operation(summary = "Get appointment by id", description = "<b>Roles:</b> ADMIN, CASHIER, DOCTOR")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR')")
    @GetMapping("/{id}")
    public ApiResponseSever<AppointmentResponse> getById(@PathVariable UUID id) {
        return ApiResponseSever.ok(appointmentService.getById(id));
    }

    @Operation(
            summary = "Search appointments (date/doctor/status)",
            description = """
                        Query appointments by date, doctorId and status.
                        <br/><b>Roles:</b> ADMIN, CASHIER, DOCTOR
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR')")
    @GetMapping
    public ApiResponseSever<Page<AppointmentResponse>> search(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(required = false)
            UUID doctorId,

            @RequestParam(required = false)
            String status,

            @RequestParam(required = false)
            WorkShift shift,

            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(appointmentService.search(date, doctorId, status, shift, pageable));
    }

    @Operation(
            summary = "Assign doctor to appointment",
            description = """
                        Assign a doctor to an appointment.
                        <br/>System checks doctor shift capacity (max patients).
                        <br/><b>Roles:</b> ADMIN, CASHIER
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned"),
            @ApiResponse(responseCode = "400", description = "Capacity full / invalid status"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @PostMapping("/{id}/assign")
    public ApiResponseSever<AppointmentResponse> assign(
            @PathVariable UUID id,
            @Valid @RequestBody AssignDoctorRequest request
    ) {
        return ApiResponseSever.ok(appointmentService.assignDoctor(id, request));
    }

    @Operation(
            summary = "Create follow-up appointment",
            description = """
                        Tạo lịch hẹn tiếp theo từ một lịch hẹn hiện tại.
                        <br/>Ví dụ: đợt 1 -> đợt 2 -> đợt 3.
                        <br/><b>Roles:</b> ADMIN, CASHIER, DOCTOR
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR')")
    @PostMapping("/{id}/follow-up")
    public ApiResponseSever<AppointmentResponse> createFollowUp(
            @PathVariable UUID id,
            @Valid @RequestBody CreateFollowUpAppointmentRequest request
    ) {
        return ApiResponseSever.ok(appointmentService.createFollowUp(id, request));
    }

    @Operation(
        summary = "Cancel appointment",
        description = "Hủy 1 lịch hoặc toàn bộ chuỗi follow-up"
        )
        @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
        @PostMapping("/{id}/cancel")
        public ApiResponseSever<Void> cancel(
                @PathVariable UUID id,
                @RequestParam(required = false) String note,
                @RequestParam(defaultValue = "false") boolean cancelAll
        ) {
        appointmentService.cancel(id, note, cancelAll);
        return ApiResponseSever.ok(null);
        }

    @Operation(
            summary = "Doctor start appointment",
            description = "Change status: ASSIGNED -> IN_PROGRESS (DOCTOR only)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Started"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/{id}/start")
    public ApiResponseSever<AppointmentResponse> start(@PathVariable UUID id) {
        return ApiResponseSever.ok(appointmentService.start(id));
    }

    @Operation(
            summary = "Doctor finish appointment",
            description = "Change status: IN_PROGRESS -> DONE (DOCTOR only)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Finished"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/{id}/finish")
    public ApiResponseSever<AppointmentResponse> finish(@PathVariable UUID id) {
        return ApiResponseSever.ok(appointmentService.finish(id));
    }

    @Operation(
            summary = "Get my appointments",
            description = "<b>Roles:</b> PATIENT"
    )
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my")
    public ApiResponseSever<Page<AppointmentResponse>> getMyAppointments(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(
                appointmentService.getMyAppointments(date, pageable)
        );
    }

    @Operation(
            summary = "Get my appointment detail",
            description = "<b>Roles:</b> PATIENT"
    )
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my/{id}")
    public ApiResponseSever<AppointmentResponse> getMyAppointmentDetail(@PathVariable UUID id) {
        return ApiResponseSever.ok(appointmentService.getMyAppointmentDetail(id));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/my")
    public ApiResponseSever<AppointmentResponse> createMy(
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        return ApiResponseSever.ok(appointmentService.createMyAppointment(request));
    }

    @Operation(
            summary = "Reschedule appointment",
            description = "Dời lịch hẹn sang ngày mới và tự dời các đợt sau"
    )
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR')")
    @PostMapping("/{id}/reschedule")
    public ApiResponseSever<AppointmentResponse> reschedule(
            @PathVariable UUID id,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate newDate
    ) {
        return ApiResponseSever.ok(appointmentService.reschedule(id, newDate));
    }

    @Operation(
        summary = "Patient cancel my appointment",
        description = "Bệnh nhân hủy lịch hẹn của chính mình"
        )
        @PreAuthorize("hasRole('PATIENT')")
        @PostMapping("/my/{id}/cancel")
        public ApiResponseSever<Void> cancelMyAppointment(
                @PathVariable UUID id,
                @RequestParam(required = false) String note
        ) {
        appointmentService.cancelMyAppointment(id, note);
        return ApiResponseSever.ok(null);
        }
   
}