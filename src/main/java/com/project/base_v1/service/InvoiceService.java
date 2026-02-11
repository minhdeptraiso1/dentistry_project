package com.project.base_v1.service;

import com.project.base_v1.dto.request.invoice.CreateInvoiceRequest;
import com.project.base_v1.dto.request.invoice.IssueInvoiceRequest;
import com.project.base_v1.dto.request.payment.AddPaymentRequest;
import com.project.base_v1.dto.response.invoice.InvoiceResponse;

import java.util.UUID;

public interface InvoiceService {
    InvoiceResponse create(CreateInvoiceRequest request);

    InvoiceResponse getById(UUID id);

    InvoiceResponse issue(UUID invoiceId, IssueInvoiceRequest request); // chốt hóa đơn

    InvoiceResponse addPayment(UUID invoiceId, AddPaymentRequest request);

    void cancel(UUID invoiceId, String note);
}
