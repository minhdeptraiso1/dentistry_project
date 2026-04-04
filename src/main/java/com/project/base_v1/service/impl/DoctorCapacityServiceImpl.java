package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.appointment.SetDoctorShiftCapacityRequest;
import com.project.base_v1.dto.response.appointment.AvailableDoctorResponse;
import com.project.base_v1.entity.DoctorShiftCapacity;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.enums.WorkShift;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.AppointmentRepository;
import com.project.base_v1.repository.DoctorShiftCapacityRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.service.DoctorCapacityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorCapacityServiceImpl implements DoctorCapacityService {

    private final DoctorShiftCapacityRepository capacityRepo;
    private final UserRepository userRepo;
    private final AppointmentRepository appointmentRepo;

    @Override
    @Transactional
    public void setCapacity(SetDoctorShiftCapacityRequest request) {

        if (request.maxPatients() == null || request.maxPatients() < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        User doctor = userRepo.findById(request.doctorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BusinessException(ErrorCode.DOCTOR_ROLE_REQUIRED);
        }

        DoctorShiftCapacity cap = capacityRepo
                .findByDoctor_IdAndWorkDateAndShift(request.doctorId(), request.workDate(), request.shift())
                .orElseGet(() -> DoctorShiftCapacity.builder()
                        .id(UUID.randomUUID())
                        .doctor(doctor)
                        .workDate(request.workDate())
                        .shift(request.shift())
                        .build()
                );

        cap.setMaxPatients(request.maxPatients());
        capacityRepo.save(cap);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableDoctorResponse> getAvailableDoctors(LocalDate workDate, WorkShift shift) {

        List<DoctorShiftCapacity> capacities =
                capacityRepo.findByWorkDateAndShift(workDate, shift);

        return capacities.stream()
                .map(cap -> {

                    User doctor = cap.getDoctor();

                    long currentPatients =
                            appointmentRepo.countAssignedInShift(
                                    doctor.getId(),
                                    workDate,
                                    shift
                            );

                    return new AvailableDoctorResponse(
                            doctor.getId(),
                            doctor.getUsername(),
                            doctor.getName(),
                            cap.getMaxPatients(),
                            (int) currentPatients
                    );

                })
                .filter(res -> res.currentPatients() < res.maxPatients()) // bác sĩ còn slot
                .toList();
    }
}