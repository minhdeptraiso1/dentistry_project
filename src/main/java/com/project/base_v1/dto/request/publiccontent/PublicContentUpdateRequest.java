package com.project.base_v1.dto.request.publiccontent;

import com.project.base_v1.enums.PublicContentType;

import java.util.List;
import java.util.UUID;

public record PublicContentUpdateRequest(
        UUID refId,
        PublicContentType refType,

        String slug,
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