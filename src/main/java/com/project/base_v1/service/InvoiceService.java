package com.project.base_v1.service;

import com.project.base_v1.dto.request.invoice.CreateInvoiceFromPrescriptionRequest;
import com.project.base_v1.dto.request.invoice.CreateInvoiceRequest;
import com.project.base_v1.dto.request.invoice.InvoiceSearchRequest;
import com.project.base_v1.dto.request.invoice.IssueInvoiceRequest;
import com.project.base_v1.dto.request.payment.AddPaymentRequest;
import com.project.base_v1.dto.response.invoice.InvoiceMyResponse;
import com.project.base_v1.dto.response.invoice.InvoiceResponse;
import com.project.base_v1.dto.response.invoice.InvoiceSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {
    InvoiceResponse create(CreateInvoiceRequest request);

    InvoiceResponse getById(UUID id);

    InvoiceResponse issue(UUID invoiceId, IssueInvoiceRequest request); // chốt hóa đơn

    InvoiceResponse addPayment(UUID invoiceId, AddPaymentRequest request);

    void cancel(UUID invoiceId, String note);

    InvoiceResponse createFromPrescription(CreateInvoiceFromPrescriptionRequest request);

    Page<InvoiceSummaryResponse> search(InvoiceSearchRequest request, Pageable pageable);

    List<InvoiceMyResponse> getMyInvoices();

    InvoiceMyResponse getMyInvoiceDetail(UUID id);
}
