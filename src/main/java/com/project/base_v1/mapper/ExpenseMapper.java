package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.expense.ExpenseResponse;
import com.project.base_v1.entity.Expense;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    ExpenseResponse toResponse(Expense e);
}