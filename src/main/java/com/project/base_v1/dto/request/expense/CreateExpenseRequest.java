package com.project.base_v1.dto.request.expense;

import com.project.base_v1.enums.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
        @NotNull ExpenseCategory category,
        @NotBlank String name,
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate expenseDate,
        String note
) {
}