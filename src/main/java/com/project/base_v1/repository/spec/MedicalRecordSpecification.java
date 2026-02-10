package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.MedicalRecord;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class MedicalRecordSpecification {

    public static Specification<MedicalRecord> hasPatientId(String patientId) {
        return (root, query, cb) -> {
            if (patientId == null || patientId.isBlank()) return null;
            return cb.equal(root.get("patient").get("id"), UUID.fromString(patientId));
        };
    }

    public static Specification<MedicalRecord> hasDoctorId(String doctorId) {
        return (root, query, cb) -> {
            if (doctorId == null || doctorId.isBlank()) return null;
            return cb.equal(root.get("doctor").get("id"), UUID.fromString(doctorId));
        };
    }

    public static Specification<MedicalRecord> visitDateFrom(Instant from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("visitDate"), from);
        };
    }

    public static Specification<MedicalRecord> visitDateTo(Instant to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("visitDate"), to);
        };
    }

    public static Specification<MedicalRecord> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            var codeLike = cb.like(cb.lower(root.get("recordCode")), kw);
            var diagnosisLike = cb.like(cb.lower(root.get("diagnosis")), kw);
            var symptomLike = cb.like(cb.lower(root.get("symptom")), kw);
            return cb.or(codeLike, diagnosisLike, symptomLike);
        };
    }
}
