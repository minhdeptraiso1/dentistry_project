package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.appointment.CreateDoctorScheduleRequest;
import com.project.base_v1.dto.request.appointment.DoctorScheduleRequestSearchRequest;
import com.project.base_v1.dto.response.appointment.DoctorScheduleRequestResponse;
import com.project.base_v1.entity.DoctorScheduleRequest;
import com.project.base_v1.entity.DoctorShiftCapacity;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.ScheduleRequestStatus;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.DoctorScheduleRequestMapper;
import com.project.base_v1.repository.DoctorScheduleRequestRepository;
import com.project.base_v1.repository.DoctorShiftCapacityRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.repository.spec.DoctorScheduleRequestSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.DoctorScheduleRequestService;
import com.project.base_v1.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorScheduleRequestServiceImpl implements DoctorScheduleRequestService {

    private final DoctorScheduleRequestRepository repo;
    private final DoctorShiftCapacityRepository capacityRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final DoctorScheduleRequestMapper mapper;


    @Override
    @Transactional(readOnly = true)
    public Page<DoctorScheduleRequestResponse> getAll(
            DoctorScheduleRequestSearchRequest request,
            Pageable pageable
    ) {

        Specification<DoctorScheduleRequest> spec = Specification.allOf(
                DoctorScheduleRequestSpecification.hasDate(request.date()),
                DoctorScheduleRequestSpecification.hasShift(request.shift()),
                DoctorScheduleRequestSpecification.hasDoctorId(request.doctorId()),
                DoctorScheduleRequestSpecification.hasStatus(request.status())
        );

        return repo.findAll(spec, pageable)
                .map(mapper::toResponse);
    }

    @Override
    public void createRequest(CreateDoctorScheduleRequest request) {

        UUID doctorId = CurrentUser.userId();

        if (doctorId == null) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        User doctor = userRepo.findById(doctorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        DoctorScheduleRequest entity = DoctorScheduleRequest.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .workDate(request.workDate())
                .shift(request.shift())
                .maxPatients(request.maxPatients())
                .status(ScheduleRequestStatus.PENDING)
                .build();

        repo.save(entity);
        // Gửi thông báo đến admin
        List<User> admins = userRepo.findByRole(UserRole.ADMIN);

        for (User admin : admins) {
            notificationService.pushToUser(
                    admin.getId(),
                    "Có lịch làm việc cần duyệt",
                    "Bác sĩ " + doctor.getUsername() +
                            " đăng ký lịch ngày " + request.workDate() +
                            " ca " + request.shift()
            );
        }
    }

    @Override
    public void approve(UUID id) {

        DoctorScheduleRequest req = repo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (req.getStatus() != ScheduleRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        boolean exists = capacityRepo.existsByDoctorAndWorkDateAndShift(
                req.getDoctor(),
                req.getWorkDate(),
                req.getShift()
        );

        if (exists) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        DoctorShiftCapacity capacity = DoctorShiftCapacity.builder()
                .id(UUID.randomUUID())
                .doctor(req.getDoctor())
                .workDate(req.getWorkDate())
                .shift(req.getShift())
                .maxPatients(req.getMaxPatients())
                .build();

        capacityRepo.save(capacity);

        req.setStatus(ScheduleRequestStatus.APPROVED);
        repo.save(req);

        notificationService.pushToUser(
                req.getDoctor().getId(),
                "Lịch làm việc đã được duyệt",
                "Lịch ngày " + req.getWorkDate() + " ca " + req.getShift() + " đã được duyệt."
        );
    }

    @Override
    public void reject(UUID id) {

        DoctorScheduleRequest req = repo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (req.getStatus() != ScheduleRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        req.setStatus(ScheduleRequestStatus.REJECTED);
        repo.save(req);


        notificationService.pushToUser(
                req.getDoctor().getId(),
                "Lịch làm việc bị từ chối",
                "Lịch ngày " + req.getWorkDate() + " ca " + req.getShift() + " đã bị từ chối."
        );
    }
}