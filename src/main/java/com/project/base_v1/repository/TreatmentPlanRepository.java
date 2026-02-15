package com.project.base_v1.repository;

import com.project.base_v1.entity.TreatmentPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, UUID>, JpaSpecificationExecutor<TreatmentPlan> {

    @EntityGraph(attributePaths = {"medicalRecord", "patient", "doctor", "items", "items.service"})
    @Query("SELECT p FROM TreatmentPlan p WHERE p.id = :id")
    Optional<TreatmentPlan> findDetailById(@Param("id") UUID id);

    @Query("""
                SELECT p.planCode
                FROM TreatmentPlan p
                ORDER BY p.planCode DESC
                LIMIT 1
            """)
    Optional<String> findLatestPlanCode();

    @Override
    @EntityGraph(attributePaths = {"patient", "doctor", "items", "items.service"})
    Page<TreatmentPlan> findAll(Specification<TreatmentPlan> spec, Pageable pageable);

}
