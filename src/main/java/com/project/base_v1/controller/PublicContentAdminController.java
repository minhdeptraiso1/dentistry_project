package com.project.base_v1.controller;

import com.project.base_v1.dto.request.publiccontent.PublicContentCreateRequest;
import com.project.base_v1.dto.request.publiccontent.PublicContentSearchRequest;
import com.project.base_v1.dto.request.publiccontent.PublicContentUpdateRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.publiccontent.PublicContentResponse;
import com.project.base_v1.dto.response.publiccontent.PublicContentSummaryResponse;
import com.project.base_v1.service.PublicContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/public-contents")
public class PublicContentAdminController {

    private final PublicContentService publicContentService;

    @Operation(summary = "Search public contents", description = "Filter by refType, active, featured, keyword, refId")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping
    public ApiResponseSever<Page<PublicContentSummaryResponse>> search(
            @ParameterObject PublicContentSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(publicContentService.search(request, pageable));
    }

    @Operation(summary = "Get public content detail for admin")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/{id}")
    public ApiResponseSever<PublicContentResponse> getById(@PathVariable UUID id) {
        return ApiResponseSever.ok(publicContentService.getAdminById(id));
    }

    @Operation(summary = "Create public content")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @PostMapping
    public ApiResponseSever<PublicContentResponse> create(@RequestBody @Valid PublicContentCreateRequest request) {
        return ApiResponseSever.ok(publicContentService.create(request));
    }

    @Operation(summary = "Update public content")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @PutMapping("/{id}")
    public ApiResponseSever<PublicContentResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid PublicContentUpdateRequest request
    ) {
        return ApiResponseSever.ok(publicContentService.update(id, request));
    }

    @Operation(summary = "Delete public content")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @DeleteMapping("/{id}")
    public ApiResponseSever<Void> delete(@PathVariable UUID id) {
        publicContentService.delete(id);
        return ApiResponseSever.ok(null);
    }
}