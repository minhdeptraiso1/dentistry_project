package com.project.base_v1.repository;

import com.project.base_v1.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

    @Query("""
                SELECT e.expenseCode
                FROM Expense e
                ORDER BY e.expenseCode DESC
                LIMIT 1
            """)
    Optional<String> findLatestExpenseCode();
}
