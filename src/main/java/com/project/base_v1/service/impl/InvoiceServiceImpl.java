package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.invoice.CreateInvoiceFromPrescriptionRequest;
import com.project.base_v1.dto.request.invoice.CreateInvoiceItemRequest;
import com.project.base_v1.dto.request.invoice.CreateInvoiceRequest;
import com.project.base_v1.dto.request.invoice.IssueInvoiceRequest;
import com.project.base_v1.dto.request.payment.AddPaymentRequest;
import com.project.base_v1.dto.response.invoice.InvoiceResponse;
import com.project.base_v1.entity.Invoice;
import com.project.base_v1.entity.InvoiceItem;
import com.project.base_v1.entity.MedicineBatch;
import com.project.base_v1.entity.Patient;
import com.project.base_v1.entity.Payment;
import com.project.base_v1.entity.ServiceCatalog;
import com.project.base_v1.entity.TreatmentItem;
import com.project.base_v1.entity.TreatmentPlan;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.InvoiceStatus;
import com.project.base_v1.enums.PrescriptionStatus;
import com.project.base_v1.enums.TreatmentItemStatus;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.InvoiceMapper;
import com.project.base_v1.repository.InvoiceRepository;
import com.project.base_v1.repository.MedicineBatchRepository;
import com.project.base_v1.repository.PatientRepository;
import com.project.base_v1.repository.PrescriptionRepository;
import com.project.base_v1.repository.ServiceCatalogRepository;
import com.project.base_v1.repository.TreatmentPlanRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.InvoiceService;
import com.project.base_v1.service.helper.InvoiceCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final PatientRepository patientRepo;
    private final TreatmentPlanRepository planRepo;
    private final ServiceCatalogRepository serviceRepo;
    private final UserRepository userRepo;
    private final PrescriptionRepository rxRepo;
    private final MedicineBatchRepository batchRepo;

    private final InvoiceCodeGenerator codeGen;
    private final InvoiceMapper mapper;

    @Override
    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest request) {

        Patient patient = patientRepo.findById(request.patientId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        User cashier = userRepo.findByUsername(CurrentUser.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // role check: CASHIER or ADMIN
        if (!(cashier.getRole() == UserRole.CASHIER || cashier.getRole() == UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Invoice invoice = Invoice.builder()
                .id(UUID.randomUUID())
                .invoiceCode(codeGen.nextCode())
                .patient(patient)
                .cashier(cashier)
                .status(InvoiceStatus.DRAFT)
                .note(request.note())
                .subtotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .build();

        // Nếu có treatmentPlan -> kéo các TreatmentItem DONE
        if (request.treatmentPlanId() != null) {
            TreatmentPlan plan = planRepo.findDetailById(request.treatmentPlanId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TREATMENT_PLAN_NOT_FOUND));

            // validate same patient
            if (!plan.getPatient().getId().equals(patient.getId())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }

            invoice.setTreatmentPlan(plan);

            List<InvoiceItem> items = new ArrayList<>();
            for (TreatmentItem ti : plan.getItems()) {
                if (ti.getStatus() != TreatmentItemStatus.DONE) continue;

                items.add(InvoiceItem.builder()
                        .id(UUID.randomUUID())
                        .invoice(invoice)
                        .service(ti.getService())
                        .itemName(ti.getItemName())
                        .serviceCode(ti.getServiceCode())
                        .serviceType(ti.getServiceType())
                        .quantity(ti.getQuantity())
                        .unitPrice(ti.getUnitPrice())
                        .discountAmount(ti.getDiscountAmount())
                        .lineTotal(ti.getLineTotal())
                        .note(ti.getNote())
                        .build());
            }

            if (items.isEmpty() && (request.items() == null || request.items().isEmpty())) {
                throw new BusinessException(ErrorCode.INVOICE_ITEMS_REQUIRED);
            }
            invoice.getItems().addAll(items);
        }

        // Nếu client gửi items thủ công (không kéo từ plan hoặc muốn bổ sung)
        if (request.items() != null && !request.items().isEmpty()) {
            invoice.getItems().addAll(toManualItems(invoice, request.items()));
        }

        // giảm giá toàn hóa đơn (optional)
        BigDecimal invoiceDiscount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        if (invoiceDiscount.signum() < 0) throw new BusinessException(ErrorCode.BAD_REQUEST);

        recalcAmounts(invoice, invoiceDiscount);

        Invoice saved = invoiceRepo.save(invoice);
        return mapper.toResponse(invoiceRepo.findDetailById(saved.getId()).orElse(saved));
    }

    @Override
    public InvoiceResponse getById(UUID id) {
        Invoice invoice = invoiceRepo.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));
        return mapper.toResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse issue(UUID invoiceId, IssueInvoiceRequest request) {
        Invoice invoice = invoiceRepo.findDetailById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException(ErrorCode.INVOICE_INVALID_STATUS);
        }

        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.INVOICE_ITEMS_REQUIRED);
        }

        if (request != null && request.note() != null) {
            invoice.setNote(request.note());
        }

        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedAt(Instant.now());

        Invoice saved = invoiceRepo.save(invoice);
        return mapper.toResponse(invoiceRepo.findDetailById(saved.getId()).orElse(saved));
    }


    @Override
    @Transactional
    public InvoiceResponse createFromPrescription(CreateInvoiceFromPrescriptionRequest request) {

        var rx = rxRepo.findDetailById(request.prescriptionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        if (rx.getStatus() != PrescriptionStatus.DISPENSED) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_INVALID_STATUS);
        }

        Patient patient = rx.getPatient();

        User cashier = userRepo.findByUsername(CurrentUser.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!(cashier.getRole() == UserRole.CASHIER || cashier.getRole() == UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        BigDecimal markup = (request.markupRate() == null || request.markupRate().signum() <= 0)
                ? new BigDecimal("1.2")
                : request.markupRate();

        Invoice invoice = Invoice.builder()
                .id(UUID.randomUUID())
                .invoiceCode(codeGen.nextCode())
                .patient(patient)
                .cashier(cashier)
                .prescription(rx)
                .status(InvoiceStatus.DRAFT)
                .note(request.note())
                .subtotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .build();

        List<InvoiceItem> items = new ArrayList<>();

        for (var pi : rx.getItems()) {

            // FIFO batch còn tồn để lấy importPrice
            List<MedicineBatch> fifo = batchRepo.findAvailableBatchesFIFO(pi.getMedicine().getId());

            BigDecimal importPrice = fifo.isEmpty()
                    ? BigDecimal.ZERO
                    : fifo.get(0).getImportPrice();

            BigDecimal unitPrice = importPrice.multiply(markup);
            int qty = (pi.getQuantity() == null || pi.getQuantity() <= 0) ? 1 : pi.getQuantity();

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            items.add(InvoiceItem.builder()
                    .id(UUID.randomUUID())
                    .invoice(invoice)
                    .service(null)
                    .itemName("Thuốc: " + pi.getMedicineName())
                    .serviceCode(pi.getMedicineCode())
                    .serviceType("MEDICINE")
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .discountAmount(BigDecimal.ZERO)
                    .lineTotal(lineTotal)
                    .note(pi.getDosage())
                    .build());
        }

        if (items.isEmpty()) {
            throw new BusinessException(ErrorCode.INVOICE_ITEMS_REQUIRED);
        }

        invoice.getItems().addAll(items);

        BigDecimal invoiceDiscount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        if (invoiceDiscount.signum() < 0) throw new BusinessException(ErrorCode.BAD_REQUEST);

        recalcAmounts(invoice, invoiceDiscount);

        Invoice saved = invoiceRepo.save(invoice);
        return mapper.toResponse(invoiceRepo.findDetailById(saved.getId()).orElse(saved));
    }


    @Override
    @Transactional
    public InvoiceResponse addPayment(UUID invoiceId, AddPaymentRequest request) {
        Invoice invoice = invoiceRepo.findDetailById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVOICE_LOCKED);
        }

        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_INVALID);
        }

        // chỉ cho payment khi đã issue hoặc đang trả
        if (!(invoice.getStatus() == InvoiceStatus.ISSUED
                || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID)) {
            throw new BusinessException(ErrorCode.INVOICE_INVALID_STATUS);
        }

        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (request.amount().compareTo(remaining) > 0) {
            throw new BusinessException(ErrorCode.PAYMENT_EXCEEDS_TOTAL);
        }

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .invoice(invoice)
                .method(request.method())
                .amount(request.amount())
                .paidAt(Instant.now())
                .reference(request.reference())
                .note(request.note())
                .build();

        invoice.getPayments().add(payment);
        invoice.setPaidAmount(invoice.getPaidAmount().add(request.amount()));

        // update status
        BigDecimal newRemaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (newRemaining.signum() == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(Instant.now());
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        return mapper.toResponse(invoiceRepo.save(invoice));
    }

    @Override
    @Transactional
    public void cancel(UUID invoiceId, String note) {
        Invoice invoice = invoiceRepo.findDetailById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException(ErrorCode.INVOICE_LOCKED);
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        if (note != null) invoice.setNote(note);

        invoiceRepo.save(invoice);
    }

    // ================= helper =================

    private List<InvoiceItem> toManualItems(Invoice invoice, List<CreateInvoiceItemRequest> req) {
        List<InvoiceItem> items = new ArrayList<>();
        for (CreateInvoiceItemRequest r : req) {
            int qty = (r.quantity() == null || r.quantity() <= 0) ? 1 : r.quantity();
            BigDecimal discount = r.discountAmount() != null ? r.discountAmount() : BigDecimal.ZERO;

            ServiceCatalog svc = null;
            if (r.serviceId() != null) {
                svc = serviceRepo.findById(r.serviceId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND));
            }

            BigDecimal lineTotal = r.unitPrice().multiply(BigDecimal.valueOf(qty)).subtract(discount);
            if (lineTotal.signum() < 0) lineTotal = BigDecimal.ZERO;

            items.add(InvoiceItem.builder()
                    .id(UUID.randomUUID())
                    .invoice(invoice)
                    .service(svc)
                    .itemName(r.itemName())
                    .serviceCode(r.serviceCode())
                    .serviceType(r.serviceType())
                    .quantity(qty)
                    .unitPrice(r.unitPrice())
                    .discountAmount(discount)
                    .lineTotal(lineTotal)
                    .note(r.note())
                    .build());
        }
        return items;
    }

    private void recalcAmounts(Invoice invoice, BigDecimal invoiceDiscount) {
        BigDecimal subtotal = invoice.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lineDiscount = invoice.getItems().stream()
                .map(InvoiceItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = lineDiscount.add(invoiceDiscount);

        BigDecimal total = subtotal.subtract(totalDiscount);
        if (total.signum() < 0) total = BigDecimal.ZERO;

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(totalDiscount);
        invoice.setTotalAmount(total);
        // paidAmount giữ nguyên (create = 0)
    }
}
