package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.invoice.*;
import com.project.base_v1.dto.request.payment.AddPaymentRequest;
import com.project.base_v1.dto.response.invoice.InvoiceMyResponse;
import com.project.base_v1.dto.response.invoice.InvoiceResponse;
import com.project.base_v1.dto.response.invoice.InvoiceSummaryResponse;
import com.project.base_v1.entity.*;
import com.project.base_v1.enums.InvoiceStatus;
import com.project.base_v1.enums.PrescriptionStatus;
import com.project.base_v1.enums.TreatmentItemStatus;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.InvoiceMapper;
import com.project.base_v1.repository.*;
import com.project.base_v1.repository.spec.InvoiceSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.InvoiceService;
import com.project.base_v1.service.NotificationService;
import com.project.base_v1.service.helper.InvoiceCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepo;

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

        // Nếu có treatmentPlan -> kéo TreatmentItem DONE + thuốc đã xuất cùng hồ sơ
        if (request.treatmentPlanId() != null) {
            TreatmentPlan plan = planRepo.findDetailById(request.treatmentPlanId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TREATMENT_PLAN_NOT_FOUND));

            // validate same patient
            if (!plan.getPatient().getId().equals(patient.getId())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
            
            if (invoiceRepo.existsByTreatmentPlan_Id(plan.getId())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }

            invoice.setTreatmentPlan(plan);

            List<InvoiceItem> treatmentItems = new ArrayList<>();

            for (TreatmentItem ti : plan.getItems()) {
                if (ti.getStatus() != TreatmentItemStatus.DONE) {
                    continue;
                }

                treatmentItems.add(InvoiceItem.builder()
                        .id(UUID.randomUUID())
                        .invoice(invoice)
                        .service(ti.getService())
                        .itemName(ti.getItemName())
                        .serviceCode(ti.getServiceCode())
                        .serviceType(ti.getServiceType())
                        .quantity(ti.getQuantity())
                        .unitPrice(ti.getUnitPrice())
                        .discountAmount(ti.getDiscountAmount() != null ? ti.getDiscountAmount() : BigDecimal.ZERO)
                        .lineTotal(ti.getLineTotal())
                        .note(ti.getNote())
                        .build());
            }

            invoice.getItems().addAll(treatmentItems);

            List<InvoiceItem> medicineItems = toDispensedPrescriptionItemsByTreatmentPlan(invoice, plan);
            invoice.getItems().addAll(medicineItems);
        }

        // Nếu client gửi items thủ công (không kéo từ plan hoặc muốn bổ sung)
        if (request.items() != null && !request.items().isEmpty()) {
            invoice.getItems().addAll(toManualItems(invoice, request.items()));
        }

        if (invoice.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.INVOICE_ITEMS_REQUIRED);
        }

        // giảm giá toàn hóa đơn (optional)
        BigDecimal invoiceDiscount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        if (invoiceDiscount.signum() < 0) throw new BusinessException(ErrorCode.BAD_REQUEST);

        recalcAmounts(invoice, invoiceDiscount);

        Invoice saved = invoiceRepo.save(invoice);

        Invoice detail = invoiceRepo.findDetailById(saved.getId()).orElse(saved);

        detail.getPayments().clear();
        detail.getPayments().addAll(paymentRepo.findByInvoiceId(detail.getId()));

        return mapper.toResponse(detail);


    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getById(UUID id) {

        Invoice invoice = invoiceRepo.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));
        invoice.getPayments().clear();
        invoice.getPayments().addAll(paymentRepo.findByInvoiceId(invoice.getId()));

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

        if (invoiceRepo.existsByPrescription_Id(rx.getId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        if (rx.getStatus() != PrescriptionStatus.DISPENSED) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_INVALID_STATUS);
        }

        Patient patient = rx.getPatient();

        User cashier = userRepo.findByUsername(CurrentUser.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!(cashier.getRole() == UserRole.CASHIER || cashier.getRole() == UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }


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

            BigDecimal unitPrice = pi.getMedicine().getSalePrice();
            if (unitPrice == null) {
                throw new BusinessException(ErrorCode.MEDICINE_PRICE_NOT_SET);
            }

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
            //
            Optional<User> patientUserOpt = userRepo.findByPatient_Id(invoice.getPatient().getId());
            patientUserOpt.ifPresent(user ->
                    notificationService.pushToUser(
                            user.getId(),
                            "Thanh toán thành công",
                            "Hóa đơn " + invoice.getInvoiceCode() + " đã được thanh toán."
                    )
            );

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

    private List<InvoiceItem> toDispensedPrescriptionItemsByTreatmentPlan(
            Invoice invoice,
            TreatmentPlan plan
    ) {
        List<InvoiceItem> items = new ArrayList<>();

        List<Prescription> prescriptions =
                rxRepo.findByMedicalRecord_IdAndPatient_IdAndStatusOrderByCreatedAtDesc(
                        plan.getMedicalRecord().getId(),
                        plan.getPatient().getId(),
                        PrescriptionStatus.DISPENSED
                );

        if (prescriptions.isEmpty()) {
            return items;
        }

        Prescription linkedPrescription = null;

        for (Prescription rx : prescriptions) {

            // Đơn thuốc đã có hóa đơn rồi thì bỏ qua, tránh tính tiền thuốc 2 lần
            if (invoiceRepo.existsByPrescription_Id(rx.getId())) {
                continue;
            }

            if (linkedPrescription == null) {
                linkedPrescription = rx;
            }

            for (var pi : rx.getItems()) {
                BigDecimal unitPrice = pi.getMedicine().getSalePrice();

                if (unitPrice == null) {
                    throw new BusinessException(ErrorCode.MEDICINE_PRICE_NOT_SET);
                }

                int qty = (pi.getQuantity() == null || pi.getQuantity() <= 0)
                        ? 1
                        : pi.getQuantity();

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
                        .note(
                                "Đơn thuốc: " + rx.getPrescriptionCode()
                                        + (pi.getDosage() != null ? " - " + pi.getDosage() : "")
                        )
                        .build());
            }
        }

        // Invoice hiện tại chỉ link được 1 prescription.
        // Link đơn đầu tiên chưa có hóa đơn.
        if (linkedPrescription != null) {
            invoice.setPrescription(linkedPrescription);
        }

        return items;
    }
    //=========================================================================================

    @Override
    public Page<InvoiceSummaryResponse> search(InvoiceSearchRequest request, Pageable pageable) {

        Specification<Invoice> spec = Specification.allOf(
                InvoiceSpecification.hasPatientId(request.patientId()),
                InvoiceSpecification.hasStatus(request.status()),
                InvoiceSpecification.createdFrom(request.fromDate()),
                InvoiceSpecification.createdTo(request.toDate())
        );

        return invoiceRepo.findAll(spec, pageable)
                .map(mapper::toSummary);
    }

    @Override
    public List<InvoiceMyResponse> getMyInvoices() {

        UUID patientId = CurrentUser.patientId();

        if (patientId == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }

        List<Invoice> invoices =
                invoiceRepo.findByPatientId(patientId);

        return invoices.stream()
                .map(mapper::toMyResponse)
                .toList();
    }

    @Override
    public InvoiceMyResponse getMyInvoiceDetail(UUID id) {

        UUID patientId = CurrentUser.patientId();

        Invoice invoice = invoiceRepo.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));

        if (!invoice.getPatient().getId().equals(patientId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return mapper.toMyResponse(invoice);
    }
}
