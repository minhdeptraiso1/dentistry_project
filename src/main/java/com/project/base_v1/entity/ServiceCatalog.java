package com.project.base_v1.entity;

import com.project.base_v1.enums.ServiceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceCatalog extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(nullable = false, unique = true)
    String code;

    @Column(nullable = false)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ServiceType type;

    @Column
    String category; // giai đoạn 1: String (enum thì đổi sau)

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "base_price", nullable = false)
    BigDecimal basePrice;

    @Column
    String unit;

    @Column(name = "duration_min")
    Integer durationMin;

    @Column(nullable = false)
    boolean active = true;

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("stepNo ASC")
    List<ServicePackageStep> steps = new ArrayList<>();
}
