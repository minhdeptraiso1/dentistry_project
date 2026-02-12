package com.project.base_v1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Medicine extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(nullable = false, unique = true)
    String code;

    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String ingredient;

    String unit;

    @Column(name = "usage_guide", columnDefinition = "TEXT")
    String usageGuide;

    @Column(nullable = false)
    boolean active = true;

    @Column(name = "sale_price")
    BigDecimal salePrice;

}
