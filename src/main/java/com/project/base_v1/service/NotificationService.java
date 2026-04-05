package com.project.base_v1.service;

import com.project.base_v1.dto.response.notification.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    Page<NotificationResponse> getMyNotifications(Pageable pageable);

    long countMyUnread();

    void markAsRead(UUID id);

    void pushToUser(UUID userId, String title, String content);
}