package com.project.base_v1.repository;

import com.project.base_v1.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, UUID> {

    Optional<PasswordResetOtp> findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(UUID userId, String purpose);

    void deleteByUserIdAndPurpose(UUID userId, String purpose);

    long countByUserIdAndPurposeAndCreatedAtAfter(UUID userId, String purpose, Instant after);
}