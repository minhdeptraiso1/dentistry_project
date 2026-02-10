package com.project.base_v1.service;

import com.project.base_v1.entity.AuditLog;

public interface AuditLogWebSocketService {
    void push(AuditLog log);
}

