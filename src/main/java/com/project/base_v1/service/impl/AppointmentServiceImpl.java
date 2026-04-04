package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.appointment.AssignDoctorRequest;
import com.project.base_v1.dto.request.appointment.CreateAppointmentRequest;
import com.project.base_v1.dto.response.appointment.AppointmentResponse;
import com.project.base_v1.entity.Appointment;
import com.project.base_v1.entity.DoctorShiftCapacity;
import com.project.base_v1.entity.Patient;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.AppointmentPriority;
import com.project.base_v1.enums.AppointmentStatus;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.enums.WorkShift;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.AppointmentMapper;
import com.project.base_v1.repository.AppointmentRepository;
import com.project.base_v1.repository.DoctorShiftCapacityRepository;
import com.project.base_v1.repository.PatientRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.repository.spec.AppointmentSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.AppointmentService;
import com.project.base_v1.service.helper.AppointmentCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo;
    private final UserRepository userRepo;
    private final DoctorShiftCapacityRepository capacityRepo;
    private final AppointmentCodeGenerator codeGen;
    private final AppointmentMapper mapper;

    @Override
    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {

        Patient patient = patientRepo.findById(request.patientId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        AppointmentPriority priority = request.priority() != null ? request.priority() : AppointmentPriority.NORMAL;

        Appointment appt = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentCode(codeGen.nextCode())
                .patient(patient)
                .workDate(request.workDate())
                .shift(request.shift())
                .status(AppointmentStatus.WAITING)
                .priority(priority)
                .note(request.note())
                .build();

        // nếu truyền doctorId => assign luôn
        if (request.doctorId() != null) {
            User doctor = userRepo.findById(request.doctorId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new BusinessException(ErrorCode.DOCTOR_ROLE_REQUIRED);
            }

            checkCapacityOrThrow(doctor.getId(), request.workDate(), request.shift());

            appt.setDoctor(doctor);
            appt.setStatus(AppointmentStatus.ASSIGNED);
        }

        return mapper.toResponse(appointmentRepo.save(appt));
    }

    @Transactional(readOnly = true)
    @Override
    public AppointmentResponse getById(UUID id) {

        Appointment appointment = appointmentRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        return mapper.toResponse(appointment);
    }

    @Override
    public Page<AppointmentResponse> search(LocalDate date, UUID doctorId, String status, WorkShift shift, Pageable pageable) {
        Specification<Appointment> spec = Specification.allOf(
                AppointmentSpecification.hasDate(date),
                AppointmentSpecification.hasDoctorId(doctorId),
                AppointmentSpecification.hasStatus(status),
                AppointmentSpecification.hasShift(shift)
        );

        return appointmentRepo.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse assignDoctor(UUID appointmentId, AssignDoctorRequest request) {

        Appointment appt = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appt.getStatus() == AppointmentStatus.CANCELLED || appt.getStatus() == AppointmentStatus.DONE) {
            throw new BusinessException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        User doctor = userRepo.findById(request.doctorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BusinessException(ErrorCode.DOCTOR_ROLE_REQUIRED);
        }

        // nếu đã assign bác sĩ trước đó và đổi bác sĩ => cần check quota của bác sĩ mới
        checkCapacityOrThrow(doctor.getId(), appt.getWorkDate(), appt.getShift());

        appt.setDoctor(doctor);
        appt.setStatus(AppointmentStatus.ASSIGNED);

        return mapper.toResponse(appointmentRepo.save(appt));
    }

    @Override
    @Transactional
    public void cancel(UUID appointmentId, String note) {

        Appointment appt = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appt.getStatus() == AppointmentStatus.DONE) {
            throw new BusinessException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        appt.setStatus(AppointmentStatus.CANCELLED);
        if (note != null) appt.setNote(note);

        // soft delete nếu em muốn “xóa khỏi danh sách”
        // appt.setDeletedAt(Instant.now());
        // appt.setDeletedBy(CurrentUser.username());

        appointmentRepo.save(appt);
    }

    // ===================== helpers =====================

    private void checkCapacityOrThrow(UUID doctorId, LocalDate date, WorkShift shift) {

        DoctorShiftCapacity cap = capacityRepo
                .findByDoctor_IdAndWorkDateAndShift(doctorId, date, shift)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_CAPACITY_NOT_SET));

        long assigned = appointmentRepo.countAssignedInShift(doctorId, date, shift);

        if (assigned >= cap.getMaxPatients()) {
            throw new BusinessException(ErrorCode.DOCTOR_SHIFT_FULL);
        }
    }

    @Override
    @Transactional
    public AppointmentResponse start(UUID appointmentId) {

        Appointment appt = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // Chỉ DOCTOR được start và chỉ start khi đã ASSIGNED
        if (appt.getStatus() != AppointmentStatus.ASSIGNED) {
            throw new BusinessException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        // đảm bảo đúng bác sĩ đang login
        String username = CurrentUser.username();
        if (appt.getDoctor() == null || appt.getDoctor().getUsername() == null
                || !appt.getDoctor().getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        appt.setStatus(AppointmentStatus.IN_PROGRESS);

        return mapper.toResponse(appointmentRepo.save(appt));
    }

    @Override
    @Transactional
    public AppointmentResponse finish(UUID appointmentId) {

        Appointment appt = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // chỉ finish khi đang IN_PROGRESS
        if (appt.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        // đảm bảo đúng bác sĩ đang login
        String username = CurrentUser.username();
        if (appt.getDoctor() == null || appt.getDoctor().getUsername() == null
                || !appt.getDoctor().getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        appt.setStatus(AppointmentStatus.DONE);

        return mapper.toResponse(appointmentRepo.save(appt));
    }

    @Override
    public Page<AppointmentResponse> getMyAppointments(
            LocalDate date,
            Pageable pageable
    ) {

        UUID patientId = CurrentUser.patientId();

        if (patientId == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }

        Specification<Appointment> spec = Specification.allOf(
                AppointmentSpecification.hasPatientId(patientId),
                AppointmentSpecification.hasDate(date)
        );

        return appointmentRepo
                .findAll(spec, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getMyAppointmentDetail(UUID id) {

        UUID patientId = CurrentUser.patientId();

        if (patientId == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }

        Appointment appointment = appointmentRepo.findByIdAndPatient_Id(id, patientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        return mapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse createMyAppointment(CreateAppointmentRequest request) {

        UUID patientId = CurrentUser.patientId();

        if (patientId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        Appointment appt = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentCode(codeGen.nextCode())
                .patient(patient)
                .workDate(request.workDate())
                .shift(request.shift())
                .status(AppointmentStatus.WAITING)
                .priority(AppointmentPriority.NORMAL)
                .note(request.note())
                .build();

        return mapper.toResponse(appointmentRepo.save(appt));
    }
}