package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.appointment.AssignDoctorRequest;
import com.project.base_v1.dto.request.appointment.CreateAppointmentRequest;
import com.project.base_v1.dto.request.appointment.CreateFollowUpAppointmentRequest;
import com.project.base_v1.dto.response.appointment.AppointmentResponse;
import com.project.base_v1.entity.*;
import com.project.base_v1.enums.AppointmentPriority;
import com.project.base_v1.enums.AppointmentStatus;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.enums.WorkShift;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.AppointmentMapper;
import com.project.base_v1.repository.*;
import com.project.base_v1.repository.spec.AppointmentSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.AppointmentService;
import com.project.base_v1.service.NotificationService;
import com.project.base_v1.service.helper.AppointmentCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
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
    private final NotificationService notificationService;
    private final TreatmentPlanRepository treatmentPlanRepo;

    @Override
    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {

        validateBookingCutoff(request.workDate(), request.shift());

        Patient patient = patientRepo.findById(request.patientId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        AppointmentPriority priority = request.priority() != null ? request.priority() : AppointmentPriority.NORMAL;

        Appointment appt = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentCode(codeGen.nextCode())
                .patient(patient)
                .parentId(null)
                .sequenceNo(1)
                .workDate(request.workDate())
                .actualDate(null)
                .shift(request.shift())
                .status(AppointmentStatus.WAITING)
                .priority(priority)
                .note(request.note())
                .build();

        Optional<User> patientUserOpt = userRepo.findByPatient_Id(patient.getId());
        patientUserOpt.ifPresent(user ->
                notificationService.pushToUser(
                        user.getId(),
                        "Lịch khám mới",
                        "Bạn có lịch khám " + appt.getAppointmentCode() + " vào ngày " + appt.getWorkDate()
                )
        );

        if (request.doctorId() != null) {
            User doctor = userRepo.findById(request.doctorId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new BusinessException(ErrorCode.DOCTOR_ROLE_REQUIRED);
            }

            checkCapacityOrThrow(doctor.getId(), request.workDate(), request.shift());

            appt.setDoctor(doctor);
            appt.setStatus(AppointmentStatus.ASSIGNED);

            notificationService.pushToUser(
                    appt.getDoctor().getId(),
                    "Có lịch khám mới",
                    "Bạn được phân công lịch khám " + appt.getAppointmentCode() + " vào ngày " + appt.getWorkDate()
            );
        }

        return mapper.toResponse(appointmentRepo.save(appt));
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getById(UUID id) {

        Appointment appointment = appointmentRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        return mapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
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

        checkCapacityOrThrow(doctor.getId(), appt.getWorkDate(), appt.getShift());

        appt.setDoctor(doctor);
        appt.setStatus(AppointmentStatus.ASSIGNED);

        Optional<User> patientUserOpt = userRepo.findByPatient_Id(appt.getPatient().getId());
        patientUserOpt.ifPresent(user ->
                notificationService.pushToUser(
                        user.getId(),
                        "Lịch khám đã được xác nhận",
                        "Lịch khám " + appt.getAppointmentCode() + " đã được phân công bác sĩ."
                )
        );

        notificationService.pushToUser(
                doctor.getId(),
                "Bạn có lịch khám mới",
                "Bạn được phân công lịch khám " + appt.getAppointmentCode()
        );

        return mapper.toResponse(appointmentRepo.save(appt));
    }

    @Override
    @Transactional
    public AppointmentResponse createFollowUp(UUID appointmentId, CreateFollowUpAppointmentRequest request) {

        validateBookingCutoff(request.workDate(), request.shift());

        Appointment parent = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        TreatmentPlan plan;

        if (request.treatmentPlanId() != null) {
            plan = treatmentPlanRepo.findById(request.treatmentPlanId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TREATMENT_PLAN_NOT_FOUND));
        } else {
            plan = parent.getTreatmentPlan();
        }

        if (plan == null) {
            throw new BusinessException(ErrorCode.TREATMENT_PLAN_NOT_FOUND);
        }

        if (!plan.getPatient().getId().equals(parent.getPatient().getId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Appointment next = Appointment.builder()
                .id(UUID.randomUUID())
                .appointmentCode(codeGen.nextCode())
                .patient(parent.getPatient())
                .treatmentPlan(plan)
                .parentId(parent.getId())
                .sequenceNo(parent.getSequenceNo() == null ? 2 : parent.getSequenceNo() + 1)
                .workDate(request.workDate())
                .actualDate(null)
                .shift(request.shift())
                .status(AppointmentStatus.WAITING)
                .priority(parent.getPriority())
                .note(request.note())
                .build();

        User assignedDoctor = null;

        if (request.doctorId() != null) {
            assignedDoctor = userRepo.findById(request.doctorId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

            if (assignedDoctor.getRole() != UserRole.DOCTOR) {
                throw new BusinessException(ErrorCode.DOCTOR_ROLE_REQUIRED);
            }

            checkCapacityOrThrow(assignedDoctor.getId(), request.workDate(), request.shift());
        } else if (parent.getDoctor() != null) {
            UUID parentDoctorId = parent.getDoctor().getId();
            if (canKeepDoctor(parentDoctorId, request.workDate(), request.shift())) {
                assignedDoctor = parent.getDoctor();
            }
        }

        if (assignedDoctor != null) {
            next.setDoctor(assignedDoctor);
            next.setStatus(AppointmentStatus.ASSIGNED);

            notificationService.pushToUser(
                    assignedDoctor.getId(),
                    "Bạn có lịch tái khám mới",
                    "Bạn được phân công lịch tái khám " + next.getAppointmentCode() + " vào ngày " + next.getWorkDate()
            );
        }

        Appointment saved = appointmentRepo.save(next);

        Optional<User> patientUserOpt = userRepo.findByPatient_Id(parent.getPatient().getId());
        patientUserOpt.ifPresent(user ->
                notificationService.pushToUser(
                        user.getId(),
                        "Đã tạo lịch hẹn tiếp theo",
                        "Lịch hẹn tiếp theo của bạn là " + saved.getAppointmentCode() + " vào ngày " + saved.getWorkDate()
                )
        );

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void cancel(UUID appointmentId, String note, boolean cancelAll) {

        Appointment appt = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appt.getStatus() == AppointmentStatus.DONE) {
            throw new BusinessException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        appt.setStatus(AppointmentStatus.CANCELLED);

        if (note != null) {
            appt.setNote(note);
        }

        appointmentRepo.save(appt);

        if (cancelAll) {
            cancelChildren(appt.getId());
        }
    }

    private void checkCapacityOrThrow(UUID doctorId, LocalDate date, WorkShift shift) {

        DoctorShiftCapacity cap = capacityRepo
                .findByDoctor_IdAndWorkDateAndShift(doctorId, date, shift)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_CAPACITY_NOT_SET));

        long assigned = appointmentRepo.countAssignedInShift(doctorId, date, shift);

        if (assigned >= cap.getMaxPatients()) {
            throw new BusinessException(ErrorCode.DOCTOR_SHIFT_FULL);
        }
    }

    private boolean canKeepDoctor(UUID doctorId, LocalDate date, WorkShift shift) {
        Optional<DoctorShiftCapacity> capOpt = capacityRepo.findByDoctor_IdAndWorkDateAndShift(doctorId, date, shift);
        if (capOpt.isEmpty()) {
            return false;
        }

        long assigned = appointmentRepo.countAssignedInShift(doctorId, date, shift);
        return assigned < capOpt.get().getMaxPatients();
    }

    private void shiftChildAppointments(UUID parentId, long delayDays) {
        if (delayDays <= 0) {
            return;
        }

        List<Appointment> children = appointmentRepo.findByParentIdOrderBySequenceNoAsc(parentId);

        for (Appointment child : children) {
            if (child.getStatus() == AppointmentStatus.CANCELLED || child.getStatus() == AppointmentStatus.DONE) {
                continue;
            }

            LocalDate newDate = child.getWorkDate().plusDays(delayDays);
            child.setWorkDate(newDate);

            if (child.getDoctor() != null) {
                UUID doctorId = child.getDoctor().getId();

                if (!canKeepDoctor(doctorId, newDate, child.getShift())) {
                    child.setDoctor(null);
                    child.setStatus(AppointmentStatus.WAITING);

                    String extraNote = "Bác sĩ cũ không còn đủ slot sau khi dời lịch tự động.";
                    child.setNote(child.getNote() == null || child.getNote().isBlank()
                            ? extraNote
                            : child.getNote() + " | " + extraNote);
                }
            }

            appointmentRepo.save(child);

            Optional<User> patientUserOpt = userRepo.findByPatient_Id(child.getPatient().getId());
            patientUserOpt.ifPresent(user ->
                    notificationService.pushToUser(
                            user.getId(),
                            "Lịch khám đã được dời",
                            "Lịch khám " + child.getAppointmentCode() + " đã được dời sang ngày " + child.getWorkDate()
                    )
            );

            shiftChildAppointments(child.getId(), delayDays);
        }
    }

    @Override
    @Transactional
    public AppointmentResponse start(UUID appointmentId) {

        Appointment appt = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appt.getStatus() != AppointmentStatus.ASSIGNED) {
            throw new BusinessException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

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

        if (appt.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        String username = CurrentUser.username();
        if (appt.getDoctor() == null || appt.getDoctor().getUsername() == null
                || !appt.getDoctor().getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        LocalDate actualDate = LocalDate.now();
        appt.setActualDate(actualDate);
        appt.setStatus(AppointmentStatus.DONE);

        Appointment saved = appointmentRepo.save(appt);

        long delayDays = ChronoUnit.DAYS.between(appt.getWorkDate(), actualDate);
        if (delayDays > 0) {
            shiftChildAppointments(appt.getId(), delayDays);
        }

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
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

        validateBookingCutoff(request.workDate(), request.shift());

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
                .parentId(null)
                .sequenceNo(1)
                .workDate(request.workDate())
                .actualDate(null)
                .shift(request.shift())
                .status(AppointmentStatus.WAITING)
                .priority(AppointmentPriority.NORMAL)
                .note(request.note())
                .build();

        return mapper.toResponse(appointmentRepo.save(appt));
    }

    @Override
    @Transactional
    public AppointmentResponse reschedule(UUID appointmentId, LocalDate newDate) {

        Appointment appt = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appt.getStatus() == AppointmentStatus.DONE) {
            throw new BusinessException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        validateBookingCutoff(newDate, appt.getShift());

        LocalDate oldDate = appt.getWorkDate();

        long delayDays = ChronoUnit.DAYS.between(oldDate, newDate);

        appt.setWorkDate(newDate);

        Appointment saved = appointmentRepo.save(appt);

        if (delayDays != 0) {
            shiftChildAppointments(appt.getId(), delayDays);
        }

        return mapper.toResponse(saved);
    }


    private void cancelChildren(UUID parentId) {

        List<Appointment> children =
                appointmentRepo.findByParentIdOrderBySequenceNoAsc(parentId);

        for (Appointment child : children) {

            child.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepo.save(child);

            cancelChildren(child.getId());
        }
    }

    private void validateBookingCutoff(LocalDate workDate, WorkShift shift) {
        if (workDate == null || shift == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (workDate.isBefore(today)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        if (workDate.isAfter(today)) {
            return;
        }

        if (shift == WorkShift.MORNING && !now.isBefore(LocalTime.of(7, 0))) {
            throw new BusinessException(ErrorCode.APPOINTMENT_BOOKING_CUTOFF_PASSED);
        }

        if (shift == WorkShift.AFTERNOON && !now.isBefore(LocalTime.of(13, 0))) {
            throw new BusinessException(ErrorCode.APPOINTMENT_BOOKING_CUTOFF_PASSED);
        }
    }

    @Override
    @Transactional
    public void cancelMyAppointment(UUID appointmentId, String note) {
        UUID patientId = CurrentUser.patientId();

        if (patientId == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }

        Appointment appt = appointmentRepo.findByIdAndPatient_Id(appointmentId, patientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appt.getStatus() == AppointmentStatus.DONE
                || appt.getStatus() == AppointmentStatus.IN_PROGRESS
                || appt.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        appt.setStatus(AppointmentStatus.CANCELLED);

        if (note != null && !note.isBlank()) {
            appt.setNote(note);
        }

        appointmentRepo.save(appt);

        if (appt.getDoctor() != null) {
            notificationService.pushToUser(
                    appt.getDoctor().getId(),
                    "Bệnh nhân đã hủy lịch",
                    "Lịch khám " + appt.getAppointmentCode() + " đã được bệnh nhân hủy."
            );
        }
    }
}