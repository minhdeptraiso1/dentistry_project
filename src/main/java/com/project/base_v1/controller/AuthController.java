package com.project.base_v1.controller;

import com.project.base_v1.dto.request.auth.ForgotPasswordRequestOtpRequest;
import com.project.base_v1.dto.request.auth.ForgotPasswordResetWithOtpRequest;
import com.project.base_v1.dto.request.auth.LoginRequest;
import com.project.base_v1.dto.request.auth.RegisterPatientRequest;
import com.project.base_v1.dto.response.auth.AuthResponse;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.service.AuthService;
import com.project.base_v1.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Auth",
        description = "Authentication APIs (JWT access token + refresh token)"
)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;
    PasswordService passwordService;

    // ===================== LOGIN =====================
    @Operation(
            summary = "Login",
            description = """
                    Authenticate by username & password.
                    <br/>
                    Returns <b>accessToken</b> (JWT) and <b>refreshToken</b>.
                    <br/>
                    <b>Public endpoint</b> (no auth required).
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login success"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "429", description = "Too many requests (rate limit)")
    })
    @PostMapping("/login")
    public ApiResponseSever<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login payload",
                    required = true
            )
            @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponseSever.ok(authService.login(request));
    }

    // ===================== REFRESH =====================
    @Operation(
            summary = "Refresh access token",
            description = """
                    Issue a new access token using refresh token.
                    <br/>
                    <b>Public endpoint</b> (no auth required).
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refresh success"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Refresh token expired / revoked")
    })
    @PostMapping("/refresh")
    public ApiResponseSever<AuthResponse> refresh(
            @Parameter(
                    description = "Refresh token obtained from /auth/login",
                    required = true
            )
            @RequestParam String refreshToken
    ) {
        return ApiResponseSever.ok(authService.refresh(refreshToken));
    }

    // ===================== LOGOUT =====================
    @Operation(
            summary = "Logout",
            description = """
                    Logout current session.
                    <br/>
                    - Revoke <b>accessToken</b> in Redis (blacklist)
                    <br/>
                    - Revoke <b>refreshToken</b> in DB (token_sessions)
                    <br/>
                    <b>Auth required</b>.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout success"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ApiResponseSever<Void> logout(
            @Parameter(
                    description = "Authorization header: Bearer <accessToken>",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIs..."
            )
            @RequestHeader("Authorization") String authHeader,

            @Parameter(
                    description = "Refresh token to revoke",
                    required = true
            )
            @RequestParam String refreshToken
    ) {
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken, refreshToken);
        return ApiResponseSever.ok(null);
    }

    @PostMapping("/register-patient")
    public void registerPatient(@RequestBody RegisterPatientRequest request) {
        authService.registerPatient(request);
    }

    @PostMapping("/forgot-password/request-otp")
    public ApiResponseSever<String> requestForgotPasswordOtp(
            @RequestBody @Valid ForgotPasswordRequestOtpRequest request
    ) {
        passwordService.requestForgotPasswordOtp(request);
        return ApiResponseSever.ok("Nếu tài khoản tồn tại, mã OTP đã được gửi về email.");
    }

    @PostMapping("/forgot-password/reset-with-otp")
    public ApiResponseSever<String> resetPasswordWithOtp(
            @RequestBody @Valid ForgotPasswordResetWithOtpRequest request
    ) {
        passwordService.resetPasswordWithOtp(request);
        return ApiResponseSever.ok("Đặt lại mật khẩu thành công.");
    }
}
