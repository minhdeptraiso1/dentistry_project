package com.project.base_v1.service.impl;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.base_v1.config.VnPayConfig;
import com.project.base_v1.dto.request.payment.CreateVnPayPaymentRequest;
import com.project.base_v1.dto.response.payment.VnPayCreatePaymentResponse;
import com.project.base_v1.dto.response.payment.VnPayReturnResponse;
import com.project.base_v1.entity.Invoice;
import com.project.base_v1.entity.Payment;
import com.project.base_v1.enums.InvoiceStatus;
import com.project.base_v1.enums.PaymentMethod;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.InvoiceRepository;
import com.project.base_v1.repository.PaymentRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.service.NotificationService;
import com.project.base_v1.service.VnPayService;
import com.project.base_v1.util.VnPayUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VnPayServiceImpl implements VnPayService {

    private final VnPayConfig props;
    private final InvoiceRepository invoiceRepo;
    private final PaymentRepository paymentRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    @Override
    public VnPayCreatePaymentResponse createPayment(HttpServletRequest req, CreateVnPayPaymentRequest dto) {
        Invoice invoice = invoiceRepo.findDetailById(dto.invoiceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));

        if (!(invoice.getStatus() == InvoiceStatus.ISSUED || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID)) {
            throw new BusinessException(ErrorCode.INVOICE_INVALID_STATUS);
        }

        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (remaining.signum() <= 0) {
            throw new BusinessException(ErrorCode.INVOICE_LOCKED);
        }

        long amount = remaining.multiply(BigDecimal.valueOf(100)).longValue();

        String bankCode = dto.bankCode();
        String locale = (dto.language() == null || dto.language().isBlank()) ? "vn" : dto.language();
        String txnRef = invoice.getId().toString();
        String ipAddr = VnPayUtil.getIpAddress(req);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", props.getVersion());
        params.put("vnp_Command", props.getCommand());
        params.put("vnp_TmnCode", props.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan hoa don: " + invoice.getInvoiceCode());
        params.put("vnp_OrderType", props.getOrderType() != null ? props.getOrderType() : "other");
        params.put("vnp_Locale", locale);
        params.put("vnp_ReturnUrl", props.getReturnUrl());
        params.put("vnp_IpAddr", ipAddr);

        if (bankCode != null && !bankCode.isBlank()) {
            params.put("vnp_BankCode", bankCode);
        }

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName)
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII))
                        .append("&");

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII))
                        .append("&");
            }
        }

        String hash = hashData.substring(0, hashData.length() - 1);
        String secureHash = VnPayUtil.hmacSHA512(props.getSecretKey(), hash);
        query.append("vnp_SecureHash=").append(secureHash);

        String paymentUrl = props.getVnpUrl() + "?" + query;

        return new VnPayCreatePaymentResponse("00", "success", paymentUrl);
    }

    private boolean validateReturn(Map<String, String> fields, String secureHash) {
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
            String name = itr.next();
            String value = fields.get(name);
            if (value != null && !value.isEmpty()) {
                sb.append(name)
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (itr.hasNext()) sb.append("&");
            }
        }

        String signValue = VnPayUtil.hmacSHA512(props.getSecretKey(), sb.toString());
        return signValue.equals(secureHash);
    }

    @Override
    @Transactional
    public VnPayReturnResponse processReturnUrl(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String name = params.nextElement();
            fields.put(name, request.getParameter(name));
        }

        String responseCode = request.getParameter("vnp_ResponseCode");
        String secureHash = request.getParameter("vnp_SecureHash");
        String invoiceIdStr = request.getParameter("vnp_TxnRef");

        boolean verified = validateReturn(new HashMap<>(fields), secureHash);

        if (!verified || !"00".equals(responseCode)) {
            return new VnPayReturnResponse(
                    false,
                    verified,
                    "Thanh toán thất bại hoặc chữ ký không hợp lệ",
                    invoiceIdStr,
                    responseCode
            );
        }

        UUID invoiceId = UUID.fromString(invoiceIdStr);

        Invoice invoice = invoiceRepo.findDetailById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            return new VnPayReturnResponse(
                    true,
                    true,
                    "Hóa đơn đã thanh toán trước đó",
                    invoiceIdStr,
                    responseCode
            );
        }

        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (remaining.signum() <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(Instant.now());
            invoiceRepo.save(invoice);

            return new VnPayReturnResponse(
                    true,
                    true,
                    "Hóa đơn đã thanh toán đủ",
                    invoiceIdStr,
                    responseCode
            );
        }

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .invoice(invoice)
                .method(PaymentMethod.TRANSFER)
                .amount(remaining)
                .paidAt(Instant.now())
                .reference(request.getParameter("vnp_TransactionNo"))
                .note("Thanh toán qua VNPAY")
                .build();

        paymentRepo.save(payment);

        invoice.setPaidAmount(invoice.getPaidAmount().add(remaining));
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());
        invoiceRepo.save(invoice);

       

        return new VnPayReturnResponse(
                true,
                true,
                "Thanh toán thành công",
                invoiceIdStr,
                responseCode
        );
    }

    @Override
    @Transactional
    public String processIpn(HttpServletRequest request) {
        // tạm thời có thể dùng chung logic verify như returnUrl
        // VNPAY thường gọi IPN phía server-server để xác nhận chắc chắn hơn
        VnPayReturnResponse result = processReturnUrl(request);
        return result.success() ? "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}"
                : "{\"RspCode\":\"99\",\"Message\":\"Confirm Fail\"}";
    }
}