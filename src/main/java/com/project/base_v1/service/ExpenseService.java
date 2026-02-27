package com.project.base_v1.service;

import com.project.base_v1.dto.request.expense.CreateExpenseRequest;
import com.project.base_v1.dto.request.expense.ExpenseSearchRequest;
import com.project.base_v1.dto.request.expense.UpdateExpenseRequest;
import com.project.base_v1.dto.response.expense.ExpenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ExpenseService {
    ExpenseResponse create(CreateExpenseRequest request);

    ExpenseResponse getById(UUID id);

    Page<ExpenseResponse> search(ExpenseSearchRequest request, Pageable pageable);

    ExpenseResponse update(UUID id, UpdateExpenseRequest request);

    void delete(UUID id);
}