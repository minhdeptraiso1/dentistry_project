package com.project.base_v1.repository;

import com.project.base_v1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    boolean existsByPatientId(UUID patientId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByPatient_Id(UUID patientId);
}
