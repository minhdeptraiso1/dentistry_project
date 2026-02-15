package com.project.base_v1.entity;

import com.project.base_v1.enums.TreatmentPlanStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "treatment_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TreatmentPlan extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(name = "plan_code", nullable = false, unique = true)
    String planCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    User doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TreatmentPlanStatus status;

    @Column(columnDefinition = "TEXT")
    String note;

    @Column(name = "total_amount", nullable = false)
    BigDecimal totalAmount;

    @Column(name = "discount_amount", nullable = false)
    BigDecimal discountAmount;

    @Column(name = "final_amount", nullable = false)
    BigDecimal finalAmount;

    @OneToMany(mappedBy = "plan",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    List<TreatmentItem> items = new ArrayList<>();

}
