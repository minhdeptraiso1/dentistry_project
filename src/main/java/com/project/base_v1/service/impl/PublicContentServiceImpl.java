package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.publiccontent.PublicContentCreateRequest;
import com.project.base_v1.dto.request.publiccontent.PublicContentSearchRequest;
import com.project.base_v1.dto.request.publiccontent.PublicContentUpdateRequest;
import com.project.base_v1.dto.response.publiccontent.PublicContentDetailResponse;
import com.project.base_v1.dto.response.publiccontent.PublicContentResponse;
import com.project.base_v1.dto.response.publiccontent.PublicContentSummaryResponse;
import com.project.base_v1.entity.PublicContent;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.PublicContentType;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.PublicContentMapper;
import com.project.base_v1.repository.MedicineRepository;
import com.project.base_v1.repository.PublicContentRepository;
import com.project.base_v1.repository.ServiceCatalogRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.repository.spec.PublicContentSpecification;
import com.project.base_v1.service.PublicContentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicContentServiceImpl implements PublicContentService {

    PublicContentRepository publicContentRepository;
    UserRepository userRepository;
    ServiceCatalogRepository serviceCatalogRepository;
    MedicineRepository medicineRepository;
    PublicContentMapper publicContentMapper;

    @Override
    @Transactional
    public PublicContentResponse create(PublicContentCreateRequest request) {
        validateRequest(request.refId(), request.refType(), request.slug(), null);

        PublicContent entity = PublicContent.builder()
                .id(UUID.randomUUID())
                .refId(request.refId())
                .refType(request.refType())
                .slug(normalize(request.slug()))
                .title(request.title().trim())
                .subtitle(normalize(request.subtitle()))
                .description(normalize(request.description()))
                .content(normalize(request.content()))
                .imageUrl(normalize(request.imageUrl()))
                .subImageUrls(joinSubImages(request.subImages()))
                .thumbnailUrl(normalize(request.thumbnailUrl()))
                .extraData(normalize(request.extraData()))
                .active(request.active() != null ? request.active() : true)
                .featured(request.featured() != null ? request.featured() : false)
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .build();

        return publicContentMapper.toResponse(publicContentRepository.save(entity));
    }

    @Override
    @Transactional
    public PublicContentResponse update(UUID id, PublicContentUpdateRequest request) {
        PublicContent entity = publicContentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_CONTENT_NOT_FOUND));

        PublicContentType nextType = request.refType() != null ? request.refType() : entity.getRefType();
        UUID nextRefId = request.refId() != null ? request.refId() : entity.getRefId();
        String nextSlug = request.slug() != null ? request.slug() : entity.getSlug();

        validateRequest(nextRefId, nextType, nextSlug, id);

        if (request.refId() != null) entity.setRefId(request.refId());
        if (request.refType() != null) entity.setRefType(request.refType());
        if (request.slug() != null) entity.setSlug(normalize(request.slug()));
        if (request.title() != null) entity.setTitle(request.title().trim());
        if (request.subtitle() != null) entity.setSubtitle(normalize(request.subtitle()));
        if (request.description() != null) entity.setDescription(normalize(request.description()));
        if (request.content() != null) entity.setContent(normalize(request.content()));
        if (request.imageUrl() != null) entity.setImageUrl(normalize(request.imageUrl()));
        if (request.subImages() != null) entity.setSubImageUrls(joinSubImages(request.subImages()));
        if (request.thumbnailUrl() != null) entity.setThumbnailUrl(normalize(request.thumbnailUrl()));
        if (request.extraData() != null) entity.setExtraData(normalize(request.extraData()));
        if (request.active() != null) entity.setActive(request.active());
        if (request.featured() != null) entity.setFeatured(request.featured());
        if (request.sortOrder() != null) entity.setSortOrder(request.sortOrder());

        return publicContentMapper.toResponse(publicContentRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PublicContent entity = publicContentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_CONTENT_NOT_FOUND));

        publicContentRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicContentResponse getAdminById(UUID id) {
        PublicContent entity = publicContentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_CONTENT_NOT_FOUND));

        return publicContentMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicContentSummaryResponse> search(PublicContentSearchRequest request, Pageable pageable) {
        Specification<PublicContent> spec = Specification.allOf(
                PublicContentSpecification.hasRefType(request.refType()),
                PublicContentSpecification.hasActive(request.active()),
                PublicContentSpecification.hasFeatured(request.featured()),
                PublicContentSpecification.hasKeyword(request.keyword()),
                PublicContentSpecification.hasRefId(request.refId())
        );

        return publicContentRepository.findAll(spec, pageable)
                .map(publicContentMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicContentSummaryResponse> getPublicList(PublicContentType type, Boolean featured) {
        Specification<PublicContent> spec = Specification.allOf(
                PublicContentSpecification.hasRefType(type),
                PublicContentSpecification.hasActive(true),
                PublicContentSpecification.hasFeatured(featured)
        );

        Sort sort = Sort.by(
                Sort.Order.desc("featured"),
                Sort.Order.asc("sortOrder"),
                Sort.Order.desc("createdAt")
        );

        return publicContentRepository.findAll(spec, sort).stream()
                .map(publicContentMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicContentDetailResponse getPublicDetailById(UUID id) {
        PublicContent entity = publicContentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_CONTENT_NOT_FOUND));

        if (Boolean.FALSE.equals(entity.getActive())) {
            throw new BusinessException(ErrorCode.PUBLIC_CONTENT_NOT_FOUND);
        }

        return publicContentMapper.toDetailResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicContentDetailResponse getPublicDetailBySlug(String slug) {
        PublicContent entity = publicContentRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_CONTENT_NOT_FOUND));

        return publicContentMapper.toDetailResponse(entity);
    }

    private void validateRequest(UUID refId, PublicContentType refType, String slug, UUID currentId) {
        if (refType == null) {
            throw new BusinessException(ErrorCode.PUBLIC_CONTENT_INVALID_TYPE);
        }

        String normalizedSlug = normalize(slug);
        if (normalizedSlug != null) {
            boolean slugExists = currentId == null
                    ? publicContentRepository.existsBySlug(normalizedSlug)
                    : publicContentRepository.existsBySlugAndIdNot(normalizedSlug, currentId);

            if (slugExists) {
                throw new BusinessException(ErrorCode.PUBLIC_CONTENT_SLUG_ALREADY_EXISTS);
            }
        }

        if (refId == null) {
            return;
        }

        validateRefIdByType(refId, refType);

        boolean refExists = currentId == null
                ? publicContentRepository.existsByRefIdAndRefType(refId, refType)
                : publicContentRepository.existsByRefIdAndRefTypeAndIdNot(refId, refType, currentId);

        if (refExists) {
            throw new BusinessException(ErrorCode.PUBLIC_CONTENT_REF_ALREADY_EXISTS);
        }
    }

    private void validateRefIdByType(UUID refId, PublicContentType refType) {
        switch (refType) {
            case DOCTOR -> {
                User user = userRepository.findById(refId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                if (user.getRole() != UserRole.DOCTOR) {
                    throw new BusinessException(ErrorCode.PUBLIC_CONTENT_INVALID_DOCTOR_REF);
                }
            }
            case SERVICE -> serviceCatalogRepository.findById(refId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND));
            case MEDICINE -> medicineRepository.findById(refId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEDICINE_NOT_FOUND));
            default -> throw new BusinessException(ErrorCode.PUBLIC_CONTENT_INVALID_TYPE);
        }
    }

    private String joinSubImages(List<String> subImages) {
        if (subImages == null || subImages.isEmpty()) {
            return null;
        }

        return subImages.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining("|"));
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}