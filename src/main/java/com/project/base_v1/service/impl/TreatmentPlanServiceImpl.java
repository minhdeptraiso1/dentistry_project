package com.project.base_v1.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.base_v1.dto.request.treatment.CreateTreatmentItemRequest;
import com.project.base_v1.dto.request.treatment.CreateTreatmentPlanRequest;
import com.project.base_v1.dto.request.treatment.UpdateTreatmentPlanRequest;
import com.project.base_v1.dto.response.treatment.TreatmentPlanResponse;
import com.project.base_v1.entity.MedicalRecord;
import com.project.base_v1.entity.ServiceCatalog;
import com.project.base_v1.entity.TreatmentItem;
import com.project.base_v1.entity.TreatmentPlan;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.TreatmentItemStatus;
import com.project.base_v1.enums.TreatmentPlanStatus;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.TreatmentPlanMapper;
import com.project.base_v1.repository.MedicalRecordRepository;
import com.project.base_v1.repository.ServiceCatalogRepository;
import com.project.base_v1.repository.TreatmentPlanRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.TreatmentPlanService;
import com.project.base_v1.service.helper.TreatmentPlanCodeGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TreatmentPlanServiceImpl implements TreatmentPlanService {

    private final TreatmentPlanRepository planRepo;
    private final MedicalRecordRepository medicalRecordRepo;
    private final ServiceCatalogRepository serviceRepo;
    private final UserRepository userRepo;
    private final TreatmentPlanCodeGenerator codeGen;
    private final TreatmentPlanMapper mapper;

    @Override
    @Transactional
    public TreatmentPlanResponse create(CreateTreatmentPlanRequest request) {

        MedicalRecord mr = medicalRecordRepo.findById(request.medicalRecordId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICAL_RECORD_NOT_FOUND));

        // doctor lấy từ medical record cho đúng nghiệp vụ
        User doctor = mr.getDoctor();

if (doctor == null || (doctor.getRole() != UserRole.DOCTOR && doctor.getRole() != UserRole.ADMIN)) {
    throw new BusinessException(ErrorCode.DOCTOR_REQUIRED);
}


        TreatmentPlan plan = TreatmentPlan.builder()
                .id(UUID.randomUUID())
                .planCode(codeGen.nextCode())
                .medicalRecord(mr)
                .patient(mr.getPatient())
                .doctor(doctor)
                .status(TreatmentPlanStatus.DRAFT)
                .note(request.note())
                .totalAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.ZERO)
                .build();

        if (request.items() != null && !request.items().isEmpty()) {
            plan.getItems().addAll(toItems(plan, request.items()));
        }

        recalcAmounts(plan);

        TreatmentPlan saved = planRepo.save(plan);

        return mapper.toResponse(planRepo.findDetailById(saved.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TREATMENT_PLAN_NOT_FOUND)));
    }

    @Override
    public TreatmentPlanResponse getById(UUID id) {
        TreatmentPlan plan = planRepo.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TREATMENT_PLAN_NOT_FOUND));
        return mapper.toResponse(plan);
    }

    @Override
    public Page<TreatmentPlanResponse> listByPatient(UUID patientId, Pageable pageable) {
        // list nhẹ: không cần fetch items full, nhưng để đơn giản em có thể dùng findAll + mapper
        Page<TreatmentPlan> page = planRepo.findAll(
                (root, query, cb) -> cb.equal(root.get("patient").get("id"), patientId),
                pageable
        );
        return page.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public TreatmentPlanResponse update(UUID id, UpdateTreatmentPlanRequest request) {

        TreatmentPlan plan = planRepo.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TREATMENT_PLAN_NOT_FOUND));

        if (request.note() != null) plan.setNote(request.note());

        // status transition rule (đơn giản giai đoạn 1)
        if (request.status() != null) {
            validateStatusTransition(plan.getStatus(), request.status());
            plan.setStatus(request.status());
        }

        // replace items nếu gửi
        if (request.items() != null) {
            if (plan.getStatus() == TreatmentPlanStatus.DONE || plan.getStatus() == TreatmentPlanStatus.CANCELLED) {
                throw new BusinessException(ErrorCode.TREATMENT_PLAN_LOCKED);
            }

            plan.getItems().clear();
            plan.getItems().addAll(toItems(plan, request.items()));
        }

        recalcAmounts(plan);

        return mapper.toResponse(planRepo.save(plan));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        TreatmentPlan plan = planRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TREATMENT_PLAN_NOT_FOUND));

        plan.setDeletedAt(Instant.now());
        plan.setDeletedBy(CurrentUser.username());
        planRepo.save(plan);
    }

    @Override
    @Transactional
    public TreatmentPlanResponse markItemDone(UUID planId, UUID itemId) {
        TreatmentPlan plan = planRepo.findDetailById(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TREATMENT_PLAN_NOT_FOUND));

        TreatmentItem item = plan.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.TREATMENT_ITEM_NOT_FOUND));

        item.setStatus(TreatmentItemStatus.DONE);

        // auto set plan status
        boolean allDone = plan.getItems().stream()
                .allMatch(i -> i.getStatus() == TreatmentItemStatus.DONE || i.getStatus() == TreatmentItemStatus.CANCELLED);

        if (allDone) plan.setStatus(TreatmentPlanStatus.DONE);
        else if (plan.getStatus() == TreatmentPlanStatus.DRAFT) plan.setStatus(TreatmentPlanStatus.IN_PROGRESS);

        planRepo.save(plan);
        return mapper.toResponse(plan);
    }

    // ================= helpers =================

    private List<TreatmentItem> toItems(TreatmentPlan plan, List<CreateTreatmentItemRequest> reqItems) {
        List<TreatmentItem> items = new ArrayList<>();

        for (CreateTreatmentItemRequest r : reqItems) {

            ServiceCatalog svc = serviceRepo.findById(r.serviceId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND));

            if (!svc.isActive()) {
                throw new BusinessException(ErrorCode.SERVICE_INACTIVE);
            }

            int qty = (r.quantity() == null || r.quantity() <= 0) ? 1 : r.quantity();
            BigDecimal unitPrice = r.unitPrice() != null ? r.unitPrice() : svc.getBasePrice();
            BigDecimal discount = r.discountAmount() != null ? r.discountAmount() : BigDecimal.ZERO;

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty)).subtract(discount);
            if (lineTotal.signum() < 0) lineTotal = BigDecimal.ZERO;

            items.add(TreatmentItem.builder()
                    .id(UUID.randomUUID())
                    .plan(plan)
                    .service(svc)
                    .itemName(svc.getName())
                    .serviceCode(svc.getCode())
                    .serviceType(svc.getType().name())
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .discountAmount(discount)
                    .lineTotal(lineTotal)
                    .toothNo(r.toothNo())
                    .toothSurface(r.toothSurface())
                    .status(TreatmentItemStatus.PLANNED)
                    .note(r.note())
                    .build());
        }
        return items;
    }

    private void recalcAmounts(TreatmentPlan plan) {
        BigDecimal total = plan.getItems().stream()
                .map(TreatmentItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = plan.getItems().stream()
                .map(TreatmentItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        plan.setTotalAmount(total.add(discount)); // total before discount
        plan.setDiscountAmount(discount);
        plan.setFinalAmount(total);
    }

    private void validateStatusTransition(TreatmentPlanStatus current, TreatmentPlanStatus next) {
        // rule đơn giản giai đoạn 1
        if (current == TreatmentPlanStatus.DONE || current == TreatmentPlanStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.TREATMENT_PLAN_LOCKED);
        }

        // không cho nhảy ngược
        if (current == TreatmentPlanStatus.IN_PROGRESS && next == TreatmentPlanStatus.DRAFT) {
            throw new BusinessException(ErrorCode.INVALID_PLAN_STATUS);
        }
    }
}
