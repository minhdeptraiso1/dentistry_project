package com.project.base_v1.repository;

import com.project.base_v1.entity.DispenseLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DispenseLogRepository extends JpaRepository<DispenseLog, UUID> {
}
