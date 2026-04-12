package com.project.base_v1.repository;

import com.project.base_v1.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository
        extends JpaRepository<Patient, UUID>, JpaSpecificationExecutor<Patient> {

    @Query("""
                SELECT p.patientCode
                FROM Patient p
                ORDER BY p.patientCode DESC
                LIMIT 1
            """)
    Optional<String> findLatestPatientCode();

    boolean existsByPhoneAndDeletedAtIsNull(String phone);

    Optional<Patient> findByPatientCode(String patientCode);

    Optional<Patient> findByPhoneAndDeletedAtIsNull(String phone);
}
