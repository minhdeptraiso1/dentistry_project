package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.Patient;
import org.springframework.data.jpa.domain.Specification;

public class PatientSpecification {

    public static Specification<Patient> phoneLike(String phone) {
        return (root, query, cb) -> {
            if (phone == null || phone.isBlank()) return null;
            return cb.like(root.get("phone"), "%" + phone.trim() + "%");
        };
    }

    public static Specification<Patient> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String kw = "%" + keyword.trim().toLowerCase() + "%";

            var nameLike = cb.like(cb.lower(root.get("fullName")), kw);
            var codeLike = cb.like(cb.lower(root.get("patientCode")), kw);

            return cb.or(nameLike, codeLike);
        };
    }
}
