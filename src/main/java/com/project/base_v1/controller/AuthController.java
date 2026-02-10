package com.project.base_v1.controller;

import com.project.base_v1.dto.request.auth.LoginRequest;
import com.project.base_v1.dto.response.auth.AuthResponse;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @Operation(security = {})
    @PostMapping("/login")
    public ApiResponseSever<AuthResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {
        return new ApiResponseSever<>(true, authService.login(request), null);
    }

    @PostMapping("/refresh")
    public ApiResponseSever<AuthResponse> refresh(
            @RequestParam String refreshToken) {

        return new ApiResponseSever<>(
                true,
                authService.refresh(refreshToken),
                null
        );
    }

    @PostMapping("/logout")
    public ApiResponseSever<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String refreshToken) {

        String accessToken = authHeader.substring(7);
        authService.logout(accessToken, refreshToken);

        return new ApiResponseSever<>(true, null, null);
    }

}
