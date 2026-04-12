package com.project.base_v1.dto.request.publiccontent;

import com.project.base_v1.enums.PublicContentType;

import java.util.UUID;

public record PublicContentSearchRequest(
        PublicContentType refType,

        Boolean active,

        Boolean featured,

        String keyword,

        UUID refId
) {
}