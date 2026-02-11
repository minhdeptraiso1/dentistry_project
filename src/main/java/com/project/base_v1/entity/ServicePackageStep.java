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
@Table(name = "service_package_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServicePackageStep extends BaseAuditEntity {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    ServiceCatalog service;

    @Column(name = "step_no", nullable = false)
    Integer stepNo;

    @Column(name = "step_name", nullable = false)
    String stepName;

    @Column(name = "step_desc", columnDefinition = "TEXT")
    String stepDesc;

    @Column(nullable = false)
    BigDecimal price;

    @Column(nullable = false)
    Integer quantity;
}
