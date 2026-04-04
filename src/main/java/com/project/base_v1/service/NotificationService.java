package com.project.base_v1.service;

import com.project.base_v1.dto.response.notification.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications();

    long countMyUnread();

    void markAsRead(UUID id);

    void pushToUser(UUID userId, String title, String content);
}