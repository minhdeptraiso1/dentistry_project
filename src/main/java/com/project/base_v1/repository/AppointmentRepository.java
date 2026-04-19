package com.project.base_v1.repository;

import com.project.base_v1.entity.Appointment;
import com.project.base_v1.enums.AppointmentStatus;
import com.project.base_v1.enums.WorkShift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {

    @Query("""
                SELECT a.appointmentCode
                FROM Appointment a
                ORDER BY a.appointmentCode DESC
                LIMIT 1
            """)
    Optional<String> findLatestCode();

    @Query("""
             SELECT COUNT(a)
             FROM Appointment a
             WHERE a.doctor.id = :doctorId
               AND a.workDate = :workDate
               AND a.shift = :shift
               AND a.status IN ('ASSIGNED','IN_PROGRESS','DONE')
            """)
    long countAssignedInShift(
            @Param("doctorId") UUID doctorId,
            @Param("workDate") LocalDate workDate,
            @Param("shift") WorkShift shift
    );

    @Override
    @EntityGraph(attributePaths = {"patient", "doctor"})
    Page<Appointment> findAll(Specification<Appointment> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"patient", "doctor"})
    Optional<Appointment> findByIdAndPatient_Id(UUID id, UUID patientId);


    List<Appointment> findByParentIdOrderBySequenceNoAsc(UUID parentId);

    List<Appointment> findByWorkDateAndReminderTodaySentFalseAndStatusIn(
            LocalDate workDate,
            Collection<AppointmentStatus> statuses
    );

    List<Appointment> findByWorkDateAndReminderTomorrowSentFalseAndStatusIn(
            LocalDate workDate,
            Collection<AppointmentStatus> statuses
    );
}