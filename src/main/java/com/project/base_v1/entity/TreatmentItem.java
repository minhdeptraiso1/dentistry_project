package com.project.base_v1.entity;

import com.project.base_v1.enums.TreatmentItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "treatment_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TreatmentItem extends BaseAuditEntity {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    TreatmentPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    ServiceCatalog service;

    // snapshot fields (để sau này service đổi tên/giá vẫn giữ lịch sử)
    @Column(name = "item_name", nullable = false)
    String itemName;

    @Column(name = "service_code")
    String serviceCode;

    @Column(name = "service_type")
    String serviceType;

    @Column(nullable = false)
    Integer quantity;

    @Column(name = "unit_price", nullable = false)
    BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false)
    BigDecimal discountAmount;

    @Column(name = "line_total", nullable = false)
    BigDecimal lineTotal;

    @Column(name = "tooth_no")
    String toothNo;

    @Column(name = "tooth_surface")
    String toothSurface;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TreatmentItemStatus status;

    @Column(columnDefinition = "TEXT")
    String note;
}
