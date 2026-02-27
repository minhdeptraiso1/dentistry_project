package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.expense.CreateExpenseRequest;
import com.project.base_v1.dto.request.expense.ExpenseSearchRequest;
import com.project.base_v1.dto.request.expense.UpdateExpenseRequest;
import com.project.base_v1.dto.response.expense.ExpenseResponse;
import com.project.base_v1.entity.Expense;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.ExpenseMapper;
import com.project.base_v1.repository.ExpenseRepository;
import com.project.base_v1.repository.spec.ExpenseSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.ExpenseService;
import com.project.base_v1.service.helper.ExpenseCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final ExpenseMapper mapper;
    private final ExpenseCodeGenerator codeGen;

    @Override
    @Transactional
    public ExpenseResponse create(CreateExpenseRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new BusinessException(ErrorCode.EXPENSE_INVALID_AMOUNT);
        }

        Expense e = Expense.builder()
                .id(UUID.randomUUID())
                .expenseCode(codeGen.nextCode())
                .category(request.category())
                .name(request.name())
                .amount(request.amount())
                .expenseDate(request.expenseDate())
                .note(request.note())
                .build();

        return mapper.toResponse(expenseRepo.save(e));
    }

    @Override
    public ExpenseResponse getById(UUID id) {
        Expense e = expenseRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));
        return mapper.toResponse(e);
    }

    @Override
    public Page<ExpenseResponse> search(ExpenseSearchRequest request, Pageable pageable) {
        Specification<Expense> spec = Specification.allOf(
                ExpenseSpecification.keywordLike(request.keyword()),
                ExpenseSpecification.hasCategory(request.category()),
                ExpenseSpecification.fromDate(request.fromDate()),
                ExpenseSpecification.toDate(request.toDate())
        );

        return expenseRepo.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ExpenseResponse update(UUID id, UpdateExpenseRequest request) {
        Expense e = expenseRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

        if (request.category() != null) e.setCategory(request.category());
        if (request.name() != null) e.setName(request.name());
        if (request.amount() != null) {
            if (request.amount().signum() <= 0) throw new BusinessException(ErrorCode.EXPENSE_INVALID_AMOUNT);
            e.setAmount(request.amount());
        }
        if (request.expenseDate() != null) e.setExpenseDate(request.expenseDate());
        if (request.note() != null) e.setNote(request.note());

        return mapper.toResponse(expenseRepo.save(e));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Expense e = expenseRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

        e.setDeletedAt(Instant.now());
        e.setDeletedBy(CurrentUser.username());
        expenseRepo.save(e);
    }
}