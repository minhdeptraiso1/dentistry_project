package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.Expense;
import com.project.base_v1.enums.ExpenseCategory;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ExpenseSpecification {

    public static Specification<Expense> keywordLike(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            var nameLike = cb.like(cb.lower(root.get("name")), kw);
            var codeLike = cb.like(cb.lower(root.get("expenseCode")), kw);
            return cb.or(nameLike, codeLike);
        };
    }

    public static Specification<Expense> hasCategory(ExpenseCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Expense> fromDate(LocalDate from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("expenseDate"), from);
    }

    public static Specification<Expense> toDate(LocalDate to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("expenseDate"), to);
    }
}