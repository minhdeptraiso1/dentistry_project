package com.project.base_v1.service;

import com.project.base_v1.dto.request.auth.ForgotPasswordRequestOtpRequest;
import com.project.base_v1.dto.request.auth.ForgotPasswordResetWithOtpRequest;
import com.project.base_v1.dto.request.user.AdminResetPasswordRequest;

import java.util.UUID;

public interface PasswordService {

    void requestForgotPasswordOtp(ForgotPasswordRequestOtpRequest request);

    void resetPasswordWithOtp(ForgotPasswordResetWithOtpRequest request);

    void adminResetPassword(UUID userId, AdminResetPasswordRequest request);
}