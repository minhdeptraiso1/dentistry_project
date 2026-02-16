package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.Invoice;
import com.project.base_v1.enums.InvoiceStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class InvoiceSpecification {

    public static Specification<Invoice> hasPatientId(UUID patientId) {
        return (root, query, cb) -> {
            if (patientId == null) return null;
            return cb.equal(root.get("patient").get("id"), patientId);
        };
    }

    public static Specification<Invoice> hasStatus(InvoiceStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Invoice> createdFrom(Instant from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<Invoice> createdTo(Instant to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
}
