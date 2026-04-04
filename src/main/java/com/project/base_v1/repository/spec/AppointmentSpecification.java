package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.Appointment;
import com.project.base_v1.enums.AppointmentStatus;
import com.project.base_v1.enums.WorkShift;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class AppointmentSpecification {

    public static Specification<Appointment> hasDate(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.equal(root.get("workDate"), date);
    }

    public static Specification<Appointment> hasShift(WorkShift shift) {
        return (root, query, cb) -> shift == null ? null : cb.equal(root.get("shift"), shift);
    }

    public static Specification<Appointment> hasDoctorId(UUID doctorId) {
        return (root, query, cb) -> doctorId == null ? null : cb.equal(root.get("doctor").get("id"), doctorId);
    }

    public static Specification<Appointment> hasPatientId(UUID patientId) {
        return (root, query, cb) ->
                patientId == null ? null : cb.equal(root.get("patient").get("id"), patientId);
    }

    public static Specification<Appointment> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return null;
            AppointmentStatus s = AppointmentStatus.valueOf(status);
            return cb.equal(root.get("status"), s);
        };
    }
}