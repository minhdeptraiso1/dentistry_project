package com.project.base_v1.entity;

import com.project.base_v1.enums.ExpenseCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Expense extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(name = "expense_code", nullable = false, unique = true)
    String expenseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ExpenseCategory category;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    LocalDate expenseDate;

    @Column(columnDefinition = "TEXT")
    String note;
}
