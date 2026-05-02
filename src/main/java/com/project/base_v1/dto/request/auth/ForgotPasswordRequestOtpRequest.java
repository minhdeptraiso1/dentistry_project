package com.project.base_v1.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequestOtpRequest {

    @NotBlank(message = "Identifier không được để trống")
    private String identifier;
}