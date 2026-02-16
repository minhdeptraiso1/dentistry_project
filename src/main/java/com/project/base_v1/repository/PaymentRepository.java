package com.project.base_v1.repository;

import com.project.base_v1.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("""
                SELECT p
                FROM Payment p
                WHERE p.invoice.id = :invoiceId
                ORDER BY p.createdAt ASC
            """)
    List<Payment> findByInvoiceId(@Param("invoiceId") UUID invoiceId);
}
