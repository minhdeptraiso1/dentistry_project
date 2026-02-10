package com.project.base_v1.service.impl;

import com.project.base_v1.entity.AuditLog;
import com.project.base_v1.service.AuditLogWebSocketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogWebSocketServiceImpl
        implements AuditLogWebSocketService {

    SimpMessagingTemplate messagingTemplate;

    @Override
    public void push(AuditLog log) {
        messagingTemplate.convertAndSend("/topic/audit-logs", log);
    }
}
