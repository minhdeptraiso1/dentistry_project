package com.project.base_v1.service;

import java.util.UUID;

public interface AuditLogService {
    void log(UUID userId, String action);
}