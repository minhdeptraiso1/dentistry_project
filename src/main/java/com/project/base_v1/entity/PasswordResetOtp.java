package com.project.base_v1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_otps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetOtp extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Column(name = "otp_code", nullable = false, length = 10)
    String otpCode;

    @Column(name = "purpose", nullable = false, length = 30)
    String purpose;

    @Column(name = "expired_at", nullable = false)
    Instant expiredAt;

    @Column(nullable = false)
    boolean used;

    @Column(name = "attempt_count", nullable = false)
    int attemptCount;
}