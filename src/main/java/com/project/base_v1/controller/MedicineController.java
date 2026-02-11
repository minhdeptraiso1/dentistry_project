package com.project.base_v1.controller;

import com.project.base_v1.dto.request.medicine.CreateMedicineRequest;
import com.project.base_v1.dto.request.medicine.ImportBatchRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.medicine.MedicineBatchResponse;
import com.project.base_v1.dto.response.medicine.MedicineResponse;
import com.project.base_v1.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Medicine", description = "APIs for medicine catalog & inventory batches")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/medicines")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MedicineController {

    MedicineService medicineService;

    @Operation(summary = "Create medicine", description = "<b>Roles:</b> ADMIN only")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponseSever<MedicineResponse> create(@Valid @RequestBody CreateMedicineRequest request) {
        return ApiResponseSever.ok(medicineService.create(request));
    }

    @Operation(summary = "Get medicine by id", description = "<b>Roles:</b> ADMIN, CASHIER, DOCTOR")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR')")
    @GetMapping("/{id}")
    public ApiResponseSever<MedicineResponse> getById(@PathVariable UUID id) {
        return ApiResponseSever.ok(medicineService.getById(id));
    }

    @Operation(summary = "Import medicine batch", description = """
            Import a batch to inventory (FIFO will use expiryDate then importDate).
            <br/><b>Roles:</b> ADMIN, CASHIER
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/batches")
    public ApiResponseSever<MedicineBatchResponse> importBatch(@Valid @RequestBody ImportBatchRequest request) {
        return ApiResponseSever.ok(medicineService.importBatch(request));
    }
}
