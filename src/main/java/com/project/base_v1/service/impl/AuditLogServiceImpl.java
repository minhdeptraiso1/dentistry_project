package com.project.base_v1.service.impl;

import com.project.base_v1.entity.AuditLog;
import com.project.base_v1.repository.AuditLogRepository;
import com.project.base_v1.service.AuditLogService;
import com.project.base_v1.service.AuditLogWebSocketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogServiceImpl implements AuditLogService {

    AuditLogRepository auditLogRepository;
    AuditLogWebSocketService webSocketService;

    @Override
    public void log(UUID userId, String action) {

        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .action(action)
                .createdAt(Instant.now())
                .build();

        auditLogRepository.save(log);

        // push realtime
        webSocketService.push(log);
    }
}
