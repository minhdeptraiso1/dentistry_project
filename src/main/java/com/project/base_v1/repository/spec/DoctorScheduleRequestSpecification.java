package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.DoctorScheduleRequest;
import com.project.base_v1.enums.ScheduleRequestStatus;
import com.project.base_v1.enums.WorkShift;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class DoctorScheduleRequestSpecification {

    public static Specification<DoctorScheduleRequest> hasDate(LocalDate date) {
        return (root, query, cb) ->
                date == null ? null : cb.equal(root.get("workDate"), date);
    }

    public static Specification<DoctorScheduleRequest> hasShift(WorkShift shift) {
        return (root, query, cb) ->
                shift == null ? null : cb.equal(root.get("shift"), shift);
    }

    public static Specification<DoctorScheduleRequest> hasDoctorId(UUID doctorId) {
        return (root, query, cb) ->
                doctorId == null ? null : cb.equal(root.get("doctor").get("id"), doctorId);
    }

    public static Specification<DoctorScheduleRequest> hasStatus(ScheduleRequestStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }
}