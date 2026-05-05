package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.PublicContent;
import com.project.base_v1.enums.PublicContentType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class PublicContentSpecification {

    public static Specification<PublicContent> hasRefType(PublicContentType refType) {
        return (root, query, cb) ->
                refType == null ? null : cb.equal(root.get("refType"), refType);
    }

    public static Specification<PublicContent> hasActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<PublicContent> hasFeatured(Boolean featured) {
        return (root, query, cb) ->
                featured == null ? null : cb.equal(root.get("featured"), featured);
    }

    public static Specification<PublicContent> hasRefId(UUID refId) {
        return (root, query, cb) ->
                refId == null ? null : cb.equal(root.get("refId"), refId);
    }

    public static Specification<PublicContent> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String likeValue = "%" + keyword.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(cb.coalesce(root.get("title"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("subtitle"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("description"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("content"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("slug"), "")), likeValue)
            );
        };
    }
}