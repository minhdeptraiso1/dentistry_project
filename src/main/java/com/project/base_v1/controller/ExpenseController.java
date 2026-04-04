package com.project.base_v1.controller;

import com.project.base_v1.dto.request.expense.CreateExpenseRequest;
import com.project.base_v1.dto.request.expense.ExpenseSearchRequest;
import com.project.base_v1.dto.request.expense.UpdateExpenseRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.expense.ExpenseResponse;
import com.project.base_v1.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Expense", description = "APIs for operating expenses")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExpenseController {

    ExpenseService expenseService;

    @Operation(summary = "Create expense", description = "<b>Roles:</b> ADMIN only")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponseSever<ExpenseResponse> create(@Valid @RequestBody CreateExpenseRequest request) {
        return ApiResponseSever.ok(expenseService.create(request));
    }

    @Operation(summary = "Get expense by id", description = "<b>Roles:</b> ADMIN")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/{id}")
    public ApiResponseSever<ExpenseResponse> getById(@PathVariable UUID id) {
        return ApiResponseSever.ok(expenseService.getById(id));
    }

    @Operation(summary = "Search expenses", description = "<b>Roles:</b> ADMIN")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping
    public ApiResponseSever<Page<ExpenseResponse>> search(
            @ParameterObject ExpenseSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(expenseService.search(request, pageable));
    }

    @Operation(summary = "Update expense", description = "<b>Roles:</b> ADMIN")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @PutMapping("/{id}")
    public ApiResponseSever<ExpenseResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateExpenseRequest request
    ) {
        return ApiResponseSever.ok(expenseService.update(id, request));
    }

    @Operation(summary = "Delete expense (soft delete)", description = "<b>Roles:</b> ADMIN")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @DeleteMapping("/{id}")
    public ApiResponseSever<Void> delete(@PathVariable UUID id) {
        expenseService.delete(id);
        return ApiResponseSever.ok(null);
    }
}