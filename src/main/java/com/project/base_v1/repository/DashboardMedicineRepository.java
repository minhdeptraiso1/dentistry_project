package com.project.base_v1.repository;

import com.project.base_v1.entity.DispenseLog;
import com.project.base_v1.repository.projection.MedicineDispensedSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DashboardMedicineRepository extends JpaRepository<DispenseLog, UUID> {

    @Query(value = """
            SELECT
                to_char(date_trunc('day', d.dispensed_at), 'YYYY-MM-DD') AS date,
                COALESCE(SUM(d.quantity), 0) AS amount
            FROM dispense_logs d
            WHERE d.deleted_at IS NULL
              AND d.dispensed_at >= :from
              AND d.dispensed_at < :to
            GROUP BY date_trunc('day', d.dispensed_at)
            ORDER BY date_trunc('day', d.dispensed_at)
            """, nativeQuery = true)
    List<Object[]> medicineDispensedByDay(Instant from, Instant to);

    @Query(value = """
            SELECT
                m.name AS category,
                COALESCE(SUM(d.quantity), 0) AS amount
            FROM dispense_logs d
            JOIN medicines m ON m.id = d.medicine_id
            WHERE d.deleted_at IS NULL
              AND m.deleted_at IS NULL
              AND d.dispensed_at >= :from
              AND d.dispensed_at < :to
            GROUP BY m.name
            ORDER BY SUM(d.quantity) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> topDispensedMedicines(Instant from, Instant to);

    @Query(value = """
            SELECT
                m.id AS medicineId,
                m.code AS medicineCode,
                m.name AS medicineName,
                COALESCE(SUM(d.quantity), 0) AS dispensedQty
            FROM dispense_logs d
            JOIN medicines m ON m.id = d.medicine_id
            WHERE d.deleted_at IS NULL
              AND m.deleted_at IS NULL
              AND d.dispensed_at >= :from
              AND d.dispensed_at < :to
            GROUP BY m.id, m.code, m.name
            ORDER BY SUM(d.quantity) DESC
            """, nativeQuery = true)
    List<MedicineDispensedSummaryProjection> dispensedByMedicine(Instant from, Instant to);
}