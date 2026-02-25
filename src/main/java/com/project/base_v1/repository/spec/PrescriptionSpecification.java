package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.Prescription;
import com.project.base_v1.enums.PrescriptionStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class PrescriptionSpecification {

    public static Specification<Prescription> hasPatientId(UUID patientId) {
        return (root, query, cb) -> {
            if (patientId == null) return null;
            return cb.equal(root.get("patient").get("id"), patientId);
        };
    }

    public static Specification<Prescription> hasDoctorId(UUID doctorId) {
        return (root, query, cb) -> {
            if (doctorId == null) return null;
            return cb.equal(root.get("doctor").get("id"), doctorId);
        };
    }

    public static Specification<Prescription> hasStatus(PrescriptionStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Prescription> createdFrom(Instant from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<Prescription> createdTo(Instant to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<Prescription> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String kw = "%" + keyword.trim().toLowerCase() + "%";

            // join để search theo patient / doctor
            var patientJoin = root.join("patient");
            var doctorJoin = root.join("doctor");

            var codeLike = cb.like(cb.lower(root.get("prescriptionCode")), kw);
            var patientCodeLike = cb.like(cb.lower(patientJoin.get("patientCode")), kw);
            var patientNameLike = cb.like(cb.lower(patientJoin.get("fullName")), kw);
            var doctorUsernameLike = cb.like(cb.lower(doctorJoin.get("username")), kw);

            // tránh duplicate do join
            query.distinct(true);

            return cb.or(codeLike, patientCodeLike, patientNameLike, doctorUsernameLike);
        };
    }
}
