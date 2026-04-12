package com.project.base_v1.repository;

import com.project.base_v1.entity.PublicContent;
import com.project.base_v1.enums.PublicContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PublicContentRepository extends JpaRepository<PublicContent, UUID>, JpaSpecificationExecutor<PublicContent> {

    Optional<PublicContent> findBySlug(String slug);

    Optional<PublicContent> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    boolean existsByRefIdAndRefType(UUID refId, PublicContentType refType);

    boolean existsByRefIdAndRefTypeAndIdNot(UUID refId, PublicContentType refType, UUID id);
}