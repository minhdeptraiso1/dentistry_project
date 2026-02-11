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

import java.util.UUID;

@Entity
@Table(name = "prescription_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionItem extends BaseAuditEntity {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    Medicine medicine;

    String medicineCode;

    @Column(nullable = false)
    String medicineName;

    String unit;

    @Column(columnDefinition = "TEXT")
    String dosage;

    @Column(nullable = false)
    Integer quantity;

    @Column(columnDefinition = "TEXT")
    String note;
}
