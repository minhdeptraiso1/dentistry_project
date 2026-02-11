package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.ServiceCatalog;
import com.project.base_v1.enums.ServiceType;
import org.springframework.data.jpa.domain.Specification;

public class ServiceCatalogSpecification {

    public static Specification<ServiceCatalog> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            var nameLike = cb.like(cb.lower(root.get("name")), kw);
            var codeLike = cb.like(cb.lower(root.get("code")), kw);
            return cb.or(nameLike, codeLike);
        };
    }

    public static Specification<ServiceCatalog> hasType(ServiceType type) {
        return (root, query, cb) -> {
            if (type == null) return null;
            return cb.equal(root.get("type"), type);
        };
    }

    public static Specification<ServiceCatalog> isActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return null;
            return cb.equal(root.get("active"), active);
        };
    }
}
