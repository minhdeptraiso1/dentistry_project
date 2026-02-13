package com.project.base_v1.repository;

import com.project.base_v1.entity.MedicineBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.UUID;

public interface DashboardCostRepository extends JpaRepository<MedicineBatch, UUID> {

    @Query(value = """
            SELECT COALESCE(SUM(e.amount), 0)
            FROM expenses e
            WHERE e.deleted_at IS NULL
              AND e.expense_date >= :from
              AND e.expense_date <= :to
            """, nativeQuery = true)
    java.math.BigDecimal operatingExpense(LocalDate from, LocalDate to);

    @Query(value = """
            SELECT COALESCE(SUM(b.import_price * b.quantity_in), 0)
            FROM medicine_batches b
            WHERE b.deleted_at IS NULL
              AND b.import_date >= :from
              AND b.import_date <= :to
            """, nativeQuery = true)
    java.math.BigDecimal medicineImportCost(LocalDate from, LocalDate to);

    @Query(value = """
            SELECT
                to_char(b.import_date, 'YYYY-MM-DD') AS date,
                COALESCE(SUM(b.import_price * b.quantity_in), 0) AS amount
            FROM medicine_batches b
            WHERE b.deleted_at IS NULL
              AND b.import_date >= :from
              AND b.import_date <= :to
            GROUP BY b.import_date
            ORDER BY b.import_date
            """, nativeQuery = true)
    java.util.List<Object[]> importCostByDay(LocalDate from, LocalDate to);
}
