package com.project.base_v1.service;

import com.project.base_v1.dto.request.payment.CreateVnPayPaymentRequest;
import com.project.base_v1.dto.response.payment.VnPayCreatePaymentResponse;
import com.project.base_v1.dto.response.payment.VnPayReturnResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface VnPayService {
    VnPayCreatePaymentResponse createPayment(HttpServletRequest request, CreateVnPayPaymentRequest dto);

    VnPayReturnResponse processReturnUrl(HttpServletRequest request);

    String processIpn(HttpServletRequest request);
}