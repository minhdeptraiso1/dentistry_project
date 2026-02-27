package com.project.base_v1.repository;

import com.project.base_v1.entity.MedicineBatch;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, UUID> {

    @Query("""
                SELECT b
                FROM MedicineBatch b
                WHERE b.medicine.id = :medicineId
                  AND b.quantityRemaining > 0
                  AND (b.expiryDate IS NULL OR b.expiryDate >= :today)
                ORDER BY b.expiryDate ASC NULLS LAST, b.importDate ASC
            """)
    List<MedicineBatch> findAvailableBatchesFIFO(@Param("medicineId") UUID medicineId,
                                                 @Param("today") java.time.LocalDate today);

    @EntityGraph(attributePaths = {"medicine"})
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
    List<MedicineBatch> findBatchesExpiredOrNear(
            @Param("today") LocalDate today,
            @Param("nearDate") LocalDate nearDate
    );

    @Query("""
                SELECT COALESCE(SUM(b.quantityRemaining), 0)
                FROM MedicineBatch b
                WHERE b.medicine.id = :medicineId
            """)
    Integer sumRemainingByMedicineId(@Param("medicineId") UUID medicineId);

    @Query("""
                SELECT b.medicine.id, COALESCE(SUM(b.quantityRemaining), 0)
                FROM MedicineBatch b
                WHERE b.medicine.id IN :ids
                GROUP BY b.medicine.id
            """)
    List<Object[]> sumRemainingByMedicineIds(@Param("ids") List<UUID> ids);

    @EntityGraph(attributePaths = {"medicine"})
    Page<MedicineBatch> findByMedicine_IdOrderByImportDateDesc(UUID medicineId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM MedicineBatch b WHERE b.id = :id")
    Optional<MedicineBatch> findByIdForUpdate(@Param("id") UUID id);


}

