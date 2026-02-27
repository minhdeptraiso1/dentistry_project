package com.project.base_v1.dto.request.expense;

import com.project.base_v1.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateExpenseRequest(
        ExpenseCategory category,
        String name,
        BigDecimal amount,
        LocalDate expenseDate,
        String note
) {
}