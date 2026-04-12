package com.project.base_v1.dto.request.publiccontent;

import com.project.base_v1.enums.PublicContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PublicContentCreateRequest(
        UUID refId,

        @NotNull
        PublicContentType refType,

        String slug,

        @NotBlank
        String title,

        String subtitle,
        String description,
        String content,

        String imageUrl,
        List<String> subImages,
        String thumbnailUrl,

        String extraData,

        Boolean active,
        Boolean featured,
        Integer sortOrder
) {
}