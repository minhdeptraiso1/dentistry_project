package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.patient.CreatePatientRequest;
import com.project.base_v1.dto.request.patient.PatientSearchRequest;
import com.project.base_v1.dto.request.patient.UpdatePatientRequest;
import com.project.base_v1.dto.response.patient.PatientResponse;
import com.project.base_v1.entity.Patient;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.PatientMapper;
import com.project.base_v1.repository.PatientRepository;
import com.project.base_v1.repository.spec.PatientSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.PatientService;
import com.project.base_v1.service.helper.PatientCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final PatientCodeGenerator patientCodeGenerator;

    @Override
    @Transactional
    public PatientResponse create(CreatePatientRequest request) {

        if (request.phone() != null && !request.phone().isBlank()
                && patientRepository.existsByPhoneAndDeletedAtIsNull(request.phone().trim())) {
            throw new BusinessException(ErrorCode.PATIENT_PHONE_DUPLICATED);
        }

        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .patientCode(patientCodeGenerator.nextCode())
                .fullName(request.fullName())
                .gender(request.gender())
                .phone(request.phone())
                .dob(request.dob())
                .address(request.address())
                .note(request.note())
                .build();

        return patientMapper.toResponse(patientRepository.save(patient));
    }


    @Override
    public PatientResponse getById(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));
        return patientMapper.toResponse(patient);
    }

    @Override
    public Page<PatientResponse> search(PatientSearchRequest request, Pageable pageable) {

        Specification<Patient> spec = Specification.allOf(
                PatientSpecification.phoneLike(request.phone()),
                PatientSpecification.keywordLike(request.keyword())
        );

        return patientRepository.findAll(spec, pageable)
                .map(patientMapper::toResponse);
    }

    @Override
    @Transactional
    public PatientResponse update(UUID id, UpdatePatientRequest request) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        if (request.fullName() != null) patient.setFullName(request.fullName());
        if (request.gender() != null) patient.setGender(request.gender());
        if (request.phone() != null) {
            String phone = request.phone().trim();
            if (!phone.isBlank()
                    && patientRepository.existsByPhoneAndDeletedAtIsNull(phone)
                    && (patient.getPhone() == null || !phone.equals(patient.getPhone()))) {
                throw new BusinessException(ErrorCode.PATIENT_PHONE_DUPLICATED);
            }
            patient.setPhone(phone);
        }

        if (request.dob() != null) patient.setDob(request.dob());
        if (request.address() != null) patient.setAddress(request.address());
        if (request.note() != null) patient.setNote(request.note());

        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        patient.setDeletedAt(Instant.now());
        patient.setDeletedBy(CurrentUser.username());

        patientRepository.save(patient);
    }

    public PatientResponse getMyProfile() {

        UUID patientId = CurrentUser.patientId();

        if (patientId == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        return patientMapper.toResponse(patient);
    }
}
