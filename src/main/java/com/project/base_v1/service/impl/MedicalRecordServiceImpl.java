package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.medical_record.CreateMedicalRecordRequest;
import com.project.base_v1.dto.request.medical_record.MedicalRecordSearchRequest;
import com.project.base_v1.dto.request.medical_record.UpdateMedicalRecordRequest;
import com.project.base_v1.dto.response.medical_record.MedicalRecordResponse;
import com.project.base_v1.entity.MedicalRecord;
import com.project.base_v1.entity.Patient;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.MedicalRecordMapper;
import com.project.base_v1.repository.MedicalRecordRepository;
import com.project.base_v1.repository.PatientRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.repository.spec.MedicalRecordSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.MedicalRecordService;
import com.project.base_v1.service.helper.MedicalRecordCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final MedicalRecordMapper medicalRecordMapper;
    private final MedicalRecordCodeGenerator codeGenerator;

    @Override
    @Transactional
    public MedicalRecordResponse create(CreateMedicalRecordRequest request) {

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        User doctor = userRepository.findById(request.doctorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_REQUIRED));

        if (doctor.getRole() != UserRole.DOCTOR && doctor.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }


        Instant visitDate = request.visitDate() != null ? request.visitDate() : Instant.now();

        MedicalRecord record = MedicalRecord.builder()
                .id(UUID.randomUUID())
                .recordCode(codeGenerator.nextCode())
                .patient(patient)
                .doctor(doctor)
                .visitDate(visitDate)
                .symptom(request.symptom())
                .diagnosis(request.diagnosis())
                .note(request.note())
                .build();

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }

    @Override
    public MedicalRecordResponse getById(UUID id) {
        MedicalRecord record = medicalRecordRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICAL_RECORD_NOT_FOUND));

        return medicalRecordMapper.toResponse(record);
    }


    @Override
    public Page<MedicalRecordResponse> search(MedicalRecordSearchRequest request, Pageable pageable) {

        Specification<MedicalRecord> spec = Specification.allOf(
                MedicalRecordSpecification.keywordLike(request.keyword()),
                MedicalRecordSpecification.hasPatientId(request.patientId()),
                MedicalRecordSpecification.hasDoctorId(request.doctorId()),
                MedicalRecordSpecification.visitDateFrom(request.fromDate()),
                MedicalRecordSpecification.visitDateTo(request.toDate())
        );

        return medicalRecordRepository.findAll(spec, pageable)
                .map(medicalRecordMapper::toResponse);
    }

    @Override
    @Transactional
    public MedicalRecordResponse update(UUID id, UpdateMedicalRecordRequest request) {

        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICAL_RECORD_NOT_FOUND));

        if (request.doctorId() != null) {
            User doctor = userRepository.findById(request.doctorId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
            record.setDoctor(doctor);
        }

        if (request.visitDate() != null) record.setVisitDate(request.visitDate());
        if (request.symptom() != null) record.setSymptom(request.symptom());
        if (request.diagnosis() != null) record.setDiagnosis(request.diagnosis());
        if (request.note() != null) record.setNote(request.note());

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICAL_RECORD_NOT_FOUND));

        record.setDeletedAt(Instant.now());
        record.setDeletedBy(CurrentUser.username());
        medicalRecordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getMyMedicalRecords() {

        UUID patientId = CurrentUser.patientId();

        if (patientId == null) {
            throw new BusinessException(ErrorCode.MEDICAL_RECORD_NOT_FOUND);
        }

        List<MedicalRecord> records =
                medicalRecordRepository.findByPatientId(patientId);

        return records.stream()
                .map(medicalRecordMapper::toResponse)
                .toList();
    }

    @Override
    public MedicalRecordResponse getMyMedicalRecordDetail(UUID id) {

        UUID patientId = CurrentUser.patientId();

        if (patientId == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }

        MedicalRecord record = medicalRecordRepository
                .findByIdAndPatient_Id(id, patientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICAL_RECORD_NOT_FOUND));

        return medicalRecordMapper.toResponse(record);
    }
}
