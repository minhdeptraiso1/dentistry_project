package com.project.base_v1.dto.request.expense;

import com.project.base_v1.enums.ExpenseCategory;

import java.time.LocalDate;

public record ExpenseSearchRequest(
        String keyword,
        ExpenseCategory category,
        LocalDate fromDate,
        LocalDate toDate
) {
}