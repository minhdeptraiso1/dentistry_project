package com.project.base_v1.dto.request.service;

import com.project.base_v1.enums.ServiceType;

public record ServiceSearchRequest(
        String keyword,     // name / code
        ServiceType type,
        Boolean active
) {
}

