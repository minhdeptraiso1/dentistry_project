package com.project.base_v1.repository;

import com.project.base_v1.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface MedicineRepository extends JpaRepository<Medicine, UUID>, JpaSpecificationExecutor<Medicine> {
    boolean existsByCode(String code);

    Optional<Medicine> findByCode(String code);

    @Query("""
                SELECT m.code
                FROM Medicine m
                ORDER BY m.code DESC
                LIMIT 1
            """)
    Optional<String> findLatestCode();
}
