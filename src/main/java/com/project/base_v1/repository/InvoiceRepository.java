package com.project.base_v1.repository;

import com.project.base_v1.entity.Invoice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

    @EntityGraph(attributePaths = {"patient", "cashier", "treatmentPlan", "items", "payments"})
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findDetailById(@Param("id") UUID id);

    @Query("""
                SELECT i.invoiceCode
                FROM Invoice i
                ORDER BY i.invoiceCode DESC
                LIMIT 1
            """)
    Optional<String> findLatestInvoiceCode();
}
