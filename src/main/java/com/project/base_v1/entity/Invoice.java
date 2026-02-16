package com.project.base_v1.entity;

import com.project.base_v1.enums.InvoiceStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invoice extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(name = "invoice_code", nullable = false, unique = true)
    String invoiceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id")
    User cashier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_plan_id")
    TreatmentPlan treatmentPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    InvoiceStatus status;

    @Column(columnDefinition = "TEXT")
    String note;

    @Column(nullable = false)
    BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = false)
    BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false)
    BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false)
    BigDecimal paidAmount;

    Instant issuedAt;
    Instant paidAt;

    @Builder.Default
    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<InvoiceItem> items = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Payment> payments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    Prescription prescription;

}
