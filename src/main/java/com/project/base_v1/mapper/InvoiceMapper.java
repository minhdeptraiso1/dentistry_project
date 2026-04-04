package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.invoice.InvoiceItemResponse;
import com.project.base_v1.dto.response.invoice.InvoiceMyResponse;
import com.project.base_v1.dto.response.invoice.InvoiceResponse;
import com.project.base_v1.dto.response.invoice.InvoiceSummaryResponse;
import com.project.base_v1.dto.response.payment.PaymentResponse;
import com.project.base_v1.entity.Invoice;
import com.project.base_v1.entity.InvoiceItem;
import com.project.base_v1.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientCode", source = "patient.patientCode")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "cashierId", source = "cashier.id")
    @Mapping(target = "cashierUsername", source = "cashier.username")
    @Mapping(target = "treatmentPlanId", source = "treatmentPlan.id")
    @Mapping(target = "prescriptionId", source = "prescription.id")
    InvoiceResponse toResponse(Invoice invoice);

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientCode", source = "patient.patientCode")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "cashierId", source = "cashier.id")
    @Mapping(target = "cashierUsername", source = "cashier.username")
    InvoiceSummaryResponse toSummary(Invoice invoice);

    @Mapping(target = "serviceId", source = "service.id")
    InvoiceItemResponse toItemResponse(InvoiceItem item);

    PaymentResponse toPaymentResponse(Payment payment);

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientCode", source = "patient.patientCode")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "items", source = "items")
    InvoiceMyResponse toMyResponse(Invoice invoice);
}
