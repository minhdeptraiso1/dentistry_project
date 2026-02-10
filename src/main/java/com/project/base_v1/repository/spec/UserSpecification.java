package com.project.base_v1.repository.spec;

import com.project.base_v1.entity.User;
import com.project.base_v1.enums.UserRole;
import org.springframework.data.jpa.domain.Specification;


public class UserSpecification {

    public static Specification<User> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("email")), like)
            );
        };
    }

    public static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) ->
                role == null ? null : cb.equal(root.get("role"), role);
    }

    public static Specification<User> isEnabled(Boolean enabled) {
        return (root, query, cb) ->
                enabled == null ? null : cb.equal(root.get("enabled"), enabled);
    }
}

