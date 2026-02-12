package com.project.base_v1.repository;

import com.project.base_v1.entity.Medicine;
import com.project.base_v1.repository.projection.MedicineStockSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InventoryReportRepository extends JpaRepository<Medicine, UUID> {

    @Query(value = """
            SELECT
                m.id AS medicineId,
                m.code AS medicineCode,
                m.name AS medicineName,
                m.unit AS unit,
                COALESCE(SUM(b.quantity_remaining), 0) AS totalRemaining,
                COALESCE(COUNT(b.id), 0) AS totalBatches,
                MIN(b.expiry_date) AS nearestExpiryDate,
                COALESCE(SUM(CASE WHEN b.expiry_date IS NOT NULL AND b.expiry_date < :today AND b.quantity_remaining > 0 THEN 1 ELSE 0 END), 0) AS expiredBatchesCount,
                COALESCE(SUM(CASE WHEN b.expiry_date IS NOT NULL AND b.expiry_date >= :today AND b.expiry_date <= :nearDate AND b.quantity_remaining > 0 THEN 1 ELSE 0 END), 0) AS nearExpiryBatchesCount
            FROM medicines m
            LEFT JOIN medicine_batches b
              ON b.medicine_id = m.id
             AND b.deleted_at IS NULL
            WHERE m.deleted_at IS NULL
              AND (:activeOnly = false OR m.active = true)
            GROUP BY m.id, m.code, m.name, m.unit
            ORDER BY m.name ASC
            """, nativeQuery = true)
    List<MedicineStockSummaryProjection> getStockSummary(LocalDate today, LocalDate nearDate, boolean activeOnly);
}
