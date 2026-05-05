package com.project.base_v1.controller;

import com.project.base_v1.dto.request.consultation.QuickConsultRequest;
import com.project.base_v1.dto.response.consultation.QuickConsultResponse;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.service.QuickConsultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/quick-consult")
public class QuickConsultController {

    private final QuickConsultService quickConsultService;

    @Operation(summary = "Quick dental consultation by keyword")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
    @PostMapping
    public ApiResponseSever<QuickConsultResponse> consult(
            @Valid @RequestBody QuickConsultRequest request
    ) {
        return ApiResponseSever.ok(quickConsultService.consult(request));
    }
}