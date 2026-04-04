package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.prescription.CreatePrescriptionItemRequest;
import com.project.base_v1.dto.request.prescription.CreatePrescriptionRequest;
import com.project.base_v1.dto.request.prescription.DispenseRequest;
import com.project.base_v1.dto.request.prescription.PrescriptionSearchRequest;
import com.project.base_v1.dto.request.prescription.UpdatePrescriptionRequest;
import com.project.base_v1.dto.response.prescription.PrescriptionResponse;
import com.project.base_v1.dto.response.prescription.PrescriptionSummaryResponse;
import com.project.base_v1.entity.DispenseLog;
import com.project.base_v1.entity.MedicalRecord;
import com.project.base_v1.entity.Medicine;
import com.project.base_v1.entity.MedicineBatch;
import com.project.base_v1.entity.Prescription;
import com.project.base_v1.entity.PrescriptionItem;
import com.project.base_v1.enums.PrescriptionStatus;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.PrescriptionMapper;
import com.project.base_v1.repository.DispenseLogRepository;
import com.project.base_v1.repository.MedicalRecordRepository;
import com.project.base_v1.repository.MedicineBatchRepository;
import com.project.base_v1.repository.MedicineRepository;
import com.project.base_v1.repository.PrescriptionRepository;
import com.project.base_v1.repository.spec.PrescriptionSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.PrescriptionService;
import com.project.base_v1.service.helper.PrescriptionCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository rxRepo;
    private final MedicalRecordRepository mrRepo;
    private final MedicineRepository medRepo;
    private final MedicineBatchRepository batchRepo;
    private final DispenseLogRepository logRepo;

    private final PrescriptionCodeGenerator codeGen;
    private final PrescriptionMapper mapper;

    @Override
    @Transactional
    public PrescriptionResponse create(CreatePrescriptionRequest request) {

        MedicalRecord mr = mrRepo.findById(request.medicalRecordId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICAL_RECORD_NOT_FOUND));

        Prescription rx = Prescription.builder()
                .id(UUID.randomUUID())
                .prescriptionCode(codeGen.nextCode())
                .medicalRecord(mr)
                .patient(mr.getPatient())
                .doctor(mr.getDoctor())
                .status(PrescriptionStatus.DRAFT)
                .note(request.note())
                .build();

        if (request.items() != null && !request.items().isEmpty()) {
            rx.getItems().addAll(toItems(rx, request.items()));
        }

        Prescription saved = rxRepo.save(rx);
        return mapper.toResponse(rxRepo.findDetailById(saved.getId()).orElse(saved));
    }

    @Override
    public PrescriptionResponse getById(UUID id) {
        Prescription rx = rxRepo.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));
        return mapper.toResponse(rx);
    }

    @Override
    @Transactional
    public PrescriptionResponse update(UUID id, UpdatePrescriptionRequest request) {

        Prescription rx = rxRepo.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        if (rx.getStatus() == PrescriptionStatus.DISPENSED || rx.getStatus() == PrescriptionStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_LOCKED);
        }

        if (request.note() != null) rx.setNote(request.note());

        if (request.status() != null) {
            // rule đơn giản: DRAFT -> ISSUED, ISSUED -> DRAFT (cho sửa) không cho sau DISPENSED
            rx.setStatus(request.status());
        }

        if (request.items() != null) {
            rx.getItems().clear();
            rx.getItems().addAll(toItems(rx, request.items()));
        }

        return mapper.toResponse(rxRepo.save(rx));
    }

    @Override
    @Transactional
    public PrescriptionResponse dispense(UUID id, DispenseRequest request) {

        Prescription rx = rxRepo.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        if (!(rx.getStatus() == PrescriptionStatus.ISSUED || rx.getStatus() == PrescriptionStatus.DRAFT)) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_INVALID_STATUS);
        }

        rx.setStatus(PrescriptionStatus.ISSUED);

        for (PrescriptionItem item : rx.getItems()) {
            int need = item.getQuantity();
            if (need <= 0)
                continue;

            List<MedicineBatch> batches = batchRepo.findAvailableBatchesFIFO(item.getMedicine().getId(), LocalDate.now());

            int remainNeed = need;
            for (MedicineBatch b : batches) {
                if (remainNeed == 0)
                    break;

                int canTake = Math.min(b.getQuantityRemaining(), remainNeed);
                if (canTake <= 0)
                    continue;

                b.setQuantityRemaining(b.getQuantityRemaining() - canTake);
                remainNeed -= canTake;


                DispenseLog log = DispenseLog.builder()
                        .id(UUID.randomUUID())
                        .prescription(rx)
                        .prescriptionItem(item)
                        .medicine(item.getMedicine())
                        .batch(b)
                        .quantity(canTake)
                        .dispensedAt(Instant.now())
                        .build();
                logRepo.save(log);
            }

            if (remainNeed > 0) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }
            Integer stock = batchRepo.sumRemainingByMedicineId(item.getMedicine().getId());
            if (stock != null && stock == 0) {
                Medicine med = item.getMedicine();
                med.setActive(false);
                medRepo.save(med);
            }

        }

        rx.setStatus(PrescriptionStatus.DISPENSED);
        rx.setUpdatedBy(CurrentUser.username());

        return mapper.toResponse(rxRepo.save(rx));
    }

    @Override
    @Transactional
    public void cancel(UUID id, String note) {
        Prescription rx = rxRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        if (rx.getStatus() == PrescriptionStatus.DISPENSED) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_LOCKED);
        }
        rx.setStatus(PrescriptionStatus.CANCELLED);
        if (note != null) rx.setNote(note);
        rxRepo.save(rx);
    }

    private List<PrescriptionItem> toItems(Prescription rx, List<CreatePrescriptionItemRequest> reqItems) {
        List<PrescriptionItem> items = new ArrayList<>();

        for (CreatePrescriptionItemRequest r : reqItems) {
            Medicine m = medRepo.findById(r.medicineId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEDICINE_NOT_FOUND));

            if (!m.isActive()) throw new BusinessException(ErrorCode.MEDICINE_INACTIVE);

            if (r.quantity() == null || r.quantity() <= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }

            items.add(PrescriptionItem.builder()
                    .id(UUID.randomUUID())
                    .prescription(rx)
                    .medicine(m)
                    .medicineCode(m.getCode())
                    .medicineName(m.getName())
                    .unit(m.getUnit())
                    .dosage(r.dosage())
                    .quantity(r.quantity())
                    .note(r.note())
                    .build());
        }

        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionSummaryResponse> search(PrescriptionSearchRequest request, Pageable pageable) {

        Specification<Prescription> spec = Specification.allOf(
                PrescriptionSpecification.keywordLike(request.keyword()),
                PrescriptionSpecification.hasPatientId(request.patientId()),
                PrescriptionSpecification.hasDoctorId(request.doctorId()),
                PrescriptionSpecification.hasStatus(request.status()),
                PrescriptionSpecification.createdFrom(request.fromDate()),
                PrescriptionSpecification.createdTo(request.toDate())
        );

        return rxRepo.findAll(spec, pageable)
                .map(mapper::toSummary);
    }


    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getMyPrescriptions() {

        UUID patientId = CurrentUser.patientId();

        if (patientId == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }

        List<Prescription> prescriptions =
                rxRepo.findByPatientId(patientId);

        return prescriptions.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public PrescriptionResponse getMyPrescriptionDetail(UUID id) {

        UUID patientId = CurrentUser.patientId();

        if (patientId == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }

        Prescription prescription = rxRepo
                .findByIdAndPatient_Id(id, patientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        return mapper.toResponse(prescription);
    }
}
