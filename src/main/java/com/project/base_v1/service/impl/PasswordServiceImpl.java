package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.auth.ForgotPasswordRequestOtpRequest;
import com.project.base_v1.dto.request.auth.ForgotPasswordResetWithOtpRequest;
import com.project.base_v1.dto.request.user.AdminResetPasswordRequest;
import com.project.base_v1.entity.PasswordResetOtp;
import com.project.base_v1.entity.User;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.PasswordResetOtpRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.service.EmailService;
import com.project.base_v1.service.PasswordService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordServiceImpl implements PasswordService {

    UserRepository userRepository;
    PasswordResetOtpRepository passwordResetOtpRepository;
    PasswordEncoder passwordEncoder;
    EmailService emailService;

    static String PURPOSE_FORGOT_PASSWORD = "FORGOT_PASSWORD";
    static int OTP_EXPIRE_MINUTES = 5;
    static int MAX_OTP_REQUEST_IN_15_MINUTES = 5;
    static int MAX_OTP_ATTEMPTS = 5;

    @Override
    @Transactional
    public void requestForgotPasswordOtp(ForgotPasswordRequestOtpRequest request) {
        User user = userRepository.findByEmailOrUsername(request.getIdentifier(), request.getIdentifier())
                .orElse(null);

        // Không để lộ tài khoản có tồn tại hay không
        if (user == null) {
            return;
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_NOT_FOUND);
        }

        long otpCount = passwordResetOtpRepository.countByUserIdAndPurposeAndCreatedAtAfter(
                user.getId(),
                PURPOSE_FORGOT_PASSWORD,
                Instant.now().minus(15, ChronoUnit.MINUTES)
        );

        if (otpCount >= MAX_OTP_REQUEST_IN_15_MINUTES) {
            throw new BusinessException(ErrorCode.TOO_MANY_OTP_REQUESTS);
        }

        passwordResetOtpRepository.deleteByUserIdAndPurpose(user.getId(), PURPOSE_FORGOT_PASSWORD);

        String otp = generateOtp6();

        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .otpCode(otp)
                .purpose(PURPOSE_FORGOT_PASSWORD)
                .expiredAt(Instant.now().plus(OTP_EXPIRE_MINUTES, ChronoUnit.MINUTES))
                .used(false)
                .attemptCount(0)
                .build();

        passwordResetOtpRepository.save(passwordResetOtp);

        String displayName = (user.getUsername() != null && !user.getUsername().isBlank())
                ? user.getUsername()
                : "Người dùng";

        emailService.sendForgotPasswordOtp(
                user.getEmail(),
                displayName,
                request.getIdentifier(),
                otp,
                OTP_EXPIRE_MINUTES
        );
    }

    @Override
    @Transactional
    public void resetPasswordWithOtp(ForgotPasswordResetWithOtpRequest request) {
        User user = userRepository.findByEmailOrUsername(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_OTP));

        PasswordResetOtp otpEntity = passwordResetOtpRepository
                .findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(user.getId(), PURPOSE_FORGOT_PASSWORD)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_OTP));

        if (otpEntity.isUsed()) {
            throw new BusinessException(ErrorCode.INVALID_OTP);
        }

        if (otpEntity.getExpiredAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }

        if (otpEntity.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }

        if (!otpEntity.getOtpCode().equals(request.getOtp())) {
            otpEntity.setAttemptCount(otpEntity.getAttemptCount() + 1);
            passwordResetOtpRepository.save(otpEntity);
            throw new BusinessException(ErrorCode.INVALID_OTP);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpEntity.setUsed(true);
        passwordResetOtpRepository.save(otpEntity);
    }

    @Override
    @Transactional
    public void adminResetPassword(UUID userId, AdminResetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private String generateOtp6() {
        SecureRandom secureRandom = new SecureRandom();
        int number = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(number);
    }
}