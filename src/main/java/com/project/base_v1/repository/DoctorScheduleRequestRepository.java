package com.project.base_v1.repository;

import com.project.base_v1.entity.DoctorScheduleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DoctorScheduleRequestRepository extends JpaRepository<DoctorScheduleRequest, UUID>, JpaSpecificationExecutor<DoctorScheduleRequest> {
}