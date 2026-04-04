package com.project.base_v1.service;

import com.project.base_v1.dto.request.auth.LoginRequest;
import com.project.base_v1.dto.request.auth.RegisterPatientRequest;
import com.project.base_v1.dto.response.auth.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void registerPatient(RegisterPatientRequest request);

    void logout(String accessToken, String refreshToken);
}

