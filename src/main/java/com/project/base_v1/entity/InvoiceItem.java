package com.project.base_v1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import java.util.UUID;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceItem extends BaseAuditEntity {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    ServiceCatalog service;

    @Column(name = "item_name", nullable = false)
    String itemName;

    String serviceCode;
    String serviceType;

    @Column(nullable = false)
    Integer quantity;

    @Column(name = "unit_price", nullable = false)
    BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false)
    BigDecimal discountAmount;

    @Column(name = "line_total", nullable = false)
    BigDecimal lineTotal;

    @Column(columnDefinition = "TEXT")
    String note;
}
