package com.project.base_v1.service;

import com.project.base_v1.dto.request.publiccontent.PublicContentCreateRequest;
import com.project.base_v1.dto.request.publiccontent.PublicContentSearchRequest;
import com.project.base_v1.dto.request.publiccontent.PublicContentUpdateRequest;
import com.project.base_v1.dto.response.publiccontent.PublicContentDetailResponse;
import com.project.base_v1.dto.response.publiccontent.PublicContentResponse;
import com.project.base_v1.dto.response.publiccontent.PublicContentSummaryResponse;
import com.project.base_v1.enums.PublicContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PublicContentService {

    PublicContentResponse create(PublicContentCreateRequest request);

    PublicContentResponse update(UUID id, PublicContentUpdateRequest request);

    void delete(UUID id);

    PublicContentResponse getAdminById(UUID id);

    Page<PublicContentSummaryResponse> search(PublicContentSearchRequest request, Pageable pageable);

    List<PublicContentSummaryResponse> getPublicList(PublicContentType type, Boolean featured);

    PublicContentDetailResponse getPublicDetailById(UUID id);

    PublicContentDetailResponse getPublicDetailBySlug(String slug);
}