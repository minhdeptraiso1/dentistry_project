package com.project.base_v1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.base_v1.dto.request.payment.CreateVnPayPaymentRequest;
import com.project.base_v1.dto.response.payment.VnPayCreatePaymentResponse;
import com.project.base_v1.dto.response.payment.VnPayReturnResponse;
import com.project.base_v1.service.VnPayService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments/vnpay")
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayService vnPayService;

    @PostMapping("/create")
    public VnPayCreatePaymentResponse createPayment(
            HttpServletRequest request,
            @RequestBody CreateVnPayPaymentRequest dto
    ) {
        return vnPayService.createPayment(request, dto);
    }

    @GetMapping("/return")
    public VnPayReturnResponse vnpayReturn(HttpServletRequest request) {
        return vnPayService.processReturnUrl(request);
    }

    @GetMapping("/ipn")
    public String vnpayIpn(HttpServletRequest request) {
        return vnPayService.processIpn(request);
    }
}