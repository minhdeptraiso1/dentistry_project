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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Patient extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(name = "patient_code", nullable = false, unique = true)
    String patientCode;

    @Column(name = "full_name", nullable = false)
    String fullName;

    @Column
    String gender;

    @Column
    String phone;

    @Column
    LocalDate dob;

    @Column
    String address;

    @Column(columnDefinition = "TEXT")
    String note;
}
