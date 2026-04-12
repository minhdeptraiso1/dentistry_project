package com.project.base_v1.controller;


import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.publiccontent.PublicContentDetailResponse;
import com.project.base_v1.dto.response.publiccontent.PublicContentSummaryResponse;
import com.project.base_v1.enums.PublicContentType;
import com.project.base_v1.service.PublicContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public-contents")
public class PublicContentPublicController {

    private final PublicContentService publicContentService;

    @Operation(summary = "Get public content list")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
    @GetMapping
    public ApiResponseSever<List<PublicContentSummaryResponse>> getList(
            @RequestParam PublicContentType type,
            @RequestParam(required = false) Boolean featured
    ) {
        return ApiResponseSever.ok(publicContentService.getPublicList(type, featured));
    }

    @Operation(summary = "Get public content detail by id")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
    @GetMapping("/{id}")
    public ApiResponseSever<PublicContentDetailResponse> getDetailById(@PathVariable UUID id) {
        return ApiResponseSever.ok(publicContentService.getPublicDetailById(id));
    }

    @Operation(summary = "Get public content detail by slug")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
    @GetMapping("/slug/{slug}")
    public ApiResponseSever<PublicContentDetailResponse> getDetailBySlug(@PathVariable String slug) {
        return ApiResponseSever.ok(publicContentService.getPublicDetailBySlug(slug));
    }
}