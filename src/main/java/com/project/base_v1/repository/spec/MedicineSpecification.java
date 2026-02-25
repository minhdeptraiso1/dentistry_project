package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.Medicine;
import org.springframework.data.jpa.domain.Specification;

public class MedicineSpecification {

    public static Specification<Medicine> hasActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return null;
            return cb.equal(root.get("active"), active);
        };
    }

    public static Specification<Medicine> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String kw = "%" + keyword.trim().toLowerCase() + "%";

            var codeLike = cb.like(cb.lower(root.get("code")), kw);
            var nameLike = cb.like(cb.lower(root.get("name")), kw);
            var ingredientLike = cb.like(cb.lower(root.get("ingredient")), kw);

            return cb.or(codeLike, nameLike, ingredientLike);
        };
    }
}
