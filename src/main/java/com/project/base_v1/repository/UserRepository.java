package com.project.base_v1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.project.base_v1.entity.User;
import com.project.base_v1.enums.UserRole;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    boolean existsByPatientId(UUID patientId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByPatient_Id(UUID patientId);

    List<User> findByRole(UserRole role);

    List<User> findByRoleAndEnabledTrueOrderByNameAsc(UserRole role);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailOrUsername(String email, String username);
}
