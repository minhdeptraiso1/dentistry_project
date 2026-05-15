package com.project.base_v1.repository;

import com.project.base_v1.entity.Prescription;
import com.project.base_v1.enums.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface
PrescriptionRepository extends JpaRepository<Prescription, UUID>, JpaSpecificationExecutor<Prescription> {

    @EntityGraph(attributePaths = {"medicalRecord", "patient", "doctor", "items", "items.medicine"})
    @Query("SELECT p FROM Prescription p WHERE p.id = :id")
    Optional<Prescription> findDetailById(@Param("id") UUID id);

    @Query("""
                SELECT p.prescriptionCode
                FROM Prescription p
                ORDER BY p.prescriptionCode DESC
                LIMIT 1
            """)
    Optional<String> findLatestCode();

    @Override
    @EntityGraph(attributePaths = {"medicalRecord", "patient", "doctor"})
    Page<Prescription> findAll(Specification<Prescription> spec, Pageable pageable);

    List<Prescription> findByPatientId(UUID patientId);

    @EntityGraph(attributePaths = {
            "medicalRecord",
            "patient",
            "doctor",
            "items",
            "items.medicine"
    })
    Optional<Prescription> findByIdAndPatient_Id(UUID id, UUID patientId);

    @EntityGraph(attributePaths = {
            "medicalRecord",
            "patient",
            "doctor",
            "items",
            "items.medicine"
    })
    @Query("""
        SELECT p FROM Prescription p
        WHERE p.medicalRecord.id = :medicalRecordId
          AND p.patient.id = :patientId
          AND p.status = :status
        ORDER BY p.createdAt DESC
        """)
    List<Prescription> findByMedicalRecord_IdAndPatient_IdAndStatusOrderByCreatedAtDesc(
            @Param("medicalRecordId") UUID medicalRecordId,
            @Param("patientId") UUID patientId,
            @Param("status") PrescriptionStatus status
    );
}
