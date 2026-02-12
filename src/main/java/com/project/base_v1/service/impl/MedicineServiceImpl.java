package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.medicine.CreateMedicineRequest;
import com.project.base_v1.dto.request.medicine.ImportBatchRequest;
import com.project.base_v1.dto.request.medicine.SetMedicinePriceRequest;
import com.project.base_v1.dto.response.medicine.MedicineBatchResponse;
import com.project.base_v1.dto.response.medicine.MedicinePriceHistoryResponse;
import com.project.base_v1.dto.response.medicine.MedicineResponse;
import com.project.base_v1.entity.Medicine;
import com.project.base_v1.entity.MedicineBatch;
import com.project.base_v1.entity.MedicinePriceHistory;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.MedicineMapper;
import com.project.base_v1.mapper.MedicinePriceHistoryMapper;
import com.project.base_v1.repository.MedicineBatchRepository;
import com.project.base_v1.repository.MedicinePriceHistoryRepository;
import com.project.base_v1.repository.MedicineRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.MedicineService;
import com.project.base_v1.service.helper.MedicineCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepo;
    private final MedicineBatchRepository batchRepo;
    private final MedicineMapper medicineMapper;
    private final MedicineCodeGenerator codeGen;
    private final MedicinePriceHistoryRepository historyRepo;
    private final MedicinePriceHistoryMapper historyMapper;

    @Override
    @Transactional
    public MedicineResponse create(CreateMedicineRequest request) {
        Medicine m = Medicine.builder()
                .id(UUID.randomUUID())
                .code(codeGen.nextCode())
                .name(request.name())
                .ingredient(request.ingredient())
                .unit(request.unit())
                .usageGuide(request.usageGuide())
                .active(true)
                .build();

        return medicineMapper.toResponse(medicineRepo.save(m));
    }

    @Override
    public MedicineResponse getById(UUID id) {
        Medicine m = medicineRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICINE_NOT_FOUND));
        return medicineMapper.toResponse(m);
    }


    @Override
    @Transactional
    public MedicineResponse setSalePrice(UUID medicineId, SetMedicinePriceRequest request) {

        if (request.newPrice() == null || request.newPrice().signum() < 0) {
            throw new BusinessException(ErrorCode.MEDICINE_PRICE_INVALID);
        }

        Medicine m = medicineRepo.findById(medicineId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICINE_NOT_FOUND));

        BigDecimal old = m.getSalePrice();
        BigDecimal newP = request.newPrice();

        // update current price
        m.setSalePrice(newP);
        medicineRepo.save(m);

        // write history
        MedicinePriceHistory h = MedicinePriceHistory.builder()
                .id(UUID.randomUUID())
                .medicine(m)
                .oldPrice(old)
                .newPrice(newP)
                .reason(request.reason())
                .changedAt(Instant.now())
                .changedBy(CurrentUser.username())
                .build();

        historyRepo.save(h);

        return medicineMapper.toResponse(m);
    }

    @Override
    public Page<MedicinePriceHistoryResponse> priceHistory(UUID medicineId, Pageable pageable) {
        // validate medicine exists (optional)
        if (!medicineRepo.existsById(medicineId)) {
            throw new BusinessException(ErrorCode.MEDICINE_NOT_FOUND);
        }
        return historyRepo.findByMedicine_IdOrderByChangedAtDesc(medicineId, pageable)
                .map(historyMapper::toResponse);
    }
    
    @Override
    @Transactional
    public MedicineBatchResponse importBatch(ImportBatchRequest request) {

        if (request.quantityIn() == null || request.quantityIn() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_BATCH_QUANTITY);
        }

        Medicine m = medicineRepo.findById(request.medicineId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICINE_NOT_FOUND));

        MedicineBatch b = MedicineBatch.builder()
                .id(UUID.randomUUID())
                .medicine(m)
                .batchNo(request.batchNo())
                .importDate(request.importDate())
                .expiryDate(request.expiryDate())
                .importPrice(request.importPrice())
                .quantityIn(request.quantityIn())
                .quantityRemaining(request.quantityIn())
                .build();

        MedicineBatch saved = batchRepo.save(b);

        return new MedicineBatchResponse(
                saved.getId(),
                m.getId(),
                m.getCode(),
                m.getName(),
                saved.getBatchNo(),
                saved.getImportDate(),
                saved.getExpiryDate(),
                saved.getImportPrice(),
                saved.getQuantityIn(),
                saved.getQuantityRemaining()
        );
    }
}
