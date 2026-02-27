package com.project.base_v1.dto.response.expense;

import com.project.base_v1.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        String expenseCode,
        ExpenseCategory category,
        String name,
        BigDecimal amount,
        LocalDate expenseDate,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}