package com.project.base_v1.entity;

import com.project.base_v1.enums.AppointmentPriority;
import com.project.base_v1.enums.AppointmentStatus;
import com.project.base_v1.enums.WorkShift;
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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Appointment extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(name = "appointment_code", nullable = false, unique = true)
    String appointmentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    User doctor;

    @Column(name = "work_date", nullable = false)
    LocalDate workDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    WorkShift shift;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AppointmentPriority priority;

    @Column(columnDefinition = "TEXT")
    String note;
}