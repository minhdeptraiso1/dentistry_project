package com.project.base_v1.repository;

import com.project.base_v1.entity.MedicineBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, UUID> {

    @Query("""
                SELECT b
                FROM MedicineBatch b
                WHERE b.medicine.id = :medicineId
                  AND b.quantityRemaining > 0
                ORDER BY b.expiryDate ASC NULLS LAST, b.importDate ASC
            """)
    List<MedicineBatch> findAvailableBatchesFIFO(@Param("medicineId") UUID medicineId);

    @Query("""
                SELECT b
                FROM MedicineBatch b
                WHERE b.quantityRemaining > 0
                  AND b.expiryDate IS NOT NULL
                  AND (
                        b.expiryDate < :today
                     OR (b.expiryDate >= :today AND b.expiryDate <= :nearDate)
                  )
                ORDER BY b.expiryDate ASC, b.importDate ASC
            """)
    List<MedicineBatch> findBatchesExpiredOrNear(LocalDate today, LocalDate nearDate);

}
