package com.project.base_v1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.base_v1.entity.MedicalRecord;

public interface MedicalRecordRepository
        extends JpaRepository<MedicalRecord, UUID>, JpaSpecificationExecutor<MedicalRecord> {

    @Query("""
                SELECT m.recordCode
                FROM MedicalRecord m
                ORDER BY m.recordCode DESC
                LIMIT 1
            """)
    Optional<String> findLatestRecordCode();

    @Query("""
                SELECT m
                FROM MedicalRecord m
                JOIN FETCH m.patient p
                JOIN FETCH m.doctor d
                WHERE m.id = :id
            """)
    Optional<MedicalRecord> findDetailById(@Param("id") UUID id);

    @Override
    @EntityGraph(attributePaths = {"patient", "doctor"})
    Page<MedicalRecord> findAll(Specification<MedicalRecord> spec, Pageable pageable);

    List<MedicalRecord> findByPatientId(UUID patientId);

    @EntityGraph(attributePaths = {
        "patient",
        "doctor" 
        })
    Optional<MedicalRecord> findByIdAndPatient_Id(UUID id, UUID patientId);

    
}
