package com.project.base_v1.controller;

import com.project.base_v1.dto.request.service.CreateServiceRequest;
import com.project.base_v1.dto.request.service.ServiceSearchRequest;
import com.project.base_v1.dto.request.service.UpdateServiceRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.service.ServiceResponse;
import com.project.base_v1.service.ServiceCatalogService;
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

import java.util.UUID;

@Tag(
        name = "Service Catalog",
        description = "APIs for service/treatment catalog (single services & packages)"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServiceCatalogController {

    ServiceCatalogService serviceCatalogService;

    // ===================== CREATE =====================
    @Operation(
            summary = "Create service catalog item",
            description = """
                    Create a service/treatment item.
                    <br/>
                    - SINGLE => code DV000001...
                    <br/>
                    - PACKAGE => code PK000001... and requires steps.
                    <br/>
                    <b>Roles:</b> ADMIN only
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponseSever<ServiceResponse> create(@Valid @RequestBody CreateServiceRequest request) {
        return ApiResponseSever.ok(serviceCatalogService.create(request));
    }

    // ===================== GET DETAIL =====================
    @Operation(
            summary = "Get service detail by id",
            description = """
                    Get service detail.
                    <br/>
                    - If PACKAGE => returns steps.
                    <br/>
                    <b>Roles:</b> ADMIN, CASHIER, DOCTOR
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','DOCTOR')")
    @GetMapping("/{id}")
    public ApiResponseSever<ServiceResponse> getById(
            @Parameter(description = "Service ID (UUID)") @PathVariable UUID id
    ) {
        return ApiResponseSever.ok(serviceCatalogService.getById(id));
    }

    // ===================== SEARCH =====================
    @Operation(
            summary = "Search services",
            description = """
                    Search by keyword/type/active with paging.
                    <br/>
                    <b>Keyword</b> matches: name OR code (LIKE).
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
    public ApiResponseSever<Page<ServiceResponse>> search(
            @ParameterObject ServiceSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(serviceCatalogService.search(request, pageable));
    }

    // ===================== UPDATE =====================
    @Operation(
            summary = "Update service",
            description = """
                    Update service info.
                    <br/>
                    - code/type cannot be changed
                    <br/>
                    - If PACKAGE and steps provided => replace all steps
                    <br/>
                    <b>Roles:</b> ADMIN only
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponseSever<ServiceResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateServiceRequest request
    ) {
        return ApiResponseSever.ok(serviceCatalogService.update(id, request));
    }

    // ===================== DELETE =====================
    @Operation(
            summary = "Delete service (soft delete)",
            description = """
                    Soft delete service.
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
        serviceCatalogService.delete(id);
        return ApiResponseSever.ok(null);
    }
}
