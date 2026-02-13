package com.project.base_v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DashboardRevenueRepository extends JpaRepository<com.project.base_v1.entity.Invoice, UUID> {

    interface RevenueSummaryProjection {
        java.math.BigDecimal getGrossRevenue();

        java.math.BigDecimal getDiscountAmount();

        java.math.BigDecimal getNetRevenue();

        java.math.BigDecimal getPaidAmount();

        java.math.BigDecimal getUnpaidAmount();
    }

    @Query(value = """
            SELECT
                COALESCE(SUM(i.subtotal), 0) AS grossRevenue,
                COALESCE(SUM(i.discount_amount), 0) AS discountAmount,
                COALESCE(SUM(i.total_amount), 0) AS netRevenue,
                COALESCE(SUM(i.paid_amount), 0) AS paidAmount,
                COALESCE(SUM(i.total_amount - i.paid_amount), 0) AS unpaidAmount
            FROM invoices i
            WHERE i.deleted_at IS NULL
              AND i.status IN ('ISSUED','PARTIALLY_PAID','PAID')
              AND i.issued_at >= :from
              AND i.issued_at < :to
            """, nativeQuery = true)
    RevenueSummaryProjection revenueSummary(Instant from, Instant to);

    @Query(value = """
            SELECT
                to_char(date_trunc('day', i.issued_at), 'YYYY-MM-DD') AS date,
                COALESCE(SUM(i.total_amount), 0) AS amount
            FROM invoices i
            WHERE i.deleted_at IS NULL
              AND i.status IN ('ISSUED','PARTIALLY_PAID','PAID')
              AND i.issued_at >= :from
              AND i.issued_at < :to
            GROUP BY date_trunc('day', i.issued_at)
            ORDER BY date_trunc('day', i.issued_at)
            """, nativeQuery = true)
    List<Object[]> revenueByDay(Instant from, Instant to);

    @Query(value = """
            SELECT
                COALESCE(ii.service_type, 'UNKNOWN') AS category,
                COALESCE(SUM(ii.line_total), 0) AS amount
            FROM invoice_items ii
            JOIN invoices i ON i.id = ii.invoice_id
            WHERE i.deleted_at IS NULL
              AND ii.deleted_at IS NULL
              AND i.status IN ('ISSUED','PARTIALLY_PAID','PAID')
              AND i.issued_at >= :from
              AND i.issued_at < :to
            GROUP BY ii.service_type
            ORDER BY SUM(ii.line_total) DESC
            """, nativeQuery = true)
    List<Object[]> revenueByServiceType(Instant from, Instant to);
}
