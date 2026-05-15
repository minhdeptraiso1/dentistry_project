package com.project.base_v1.repository;

import com.project.base_v1.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

    @EntityGraph(attributePaths = {"patient", "cashier", "treatmentPlan", "items"})
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findDetailById(@Param("id") UUID id);

    @Query(value = "SELECT invoice_code FROM invoices ORDER BY invoice_code DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestInvoiceCode();

    @Override
    @EntityGraph(attributePaths = {"patient", "cashier"})
    Page<Invoice> findAll(Specification<Invoice> spec, Pageable pageable);

    @EntityGraph(attributePaths = {
            "patient",
            "cashier",
            "items",
    })
    List<Invoice> findByPatientId(UUID patientId);

    boolean existsByPrescription_Id(UUID prescriptionId);

    boolean existsByTreatmentPlan_Id(UUID treatmentPlanId);

}

