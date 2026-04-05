package com.project.base_v1.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.base_v1.dto.response.notification.NotificationResponse;
import com.project.base_v1.entity.Notification;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.NotificationMapper;
import com.project.base_v1.repository.NotificationRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {

        UUID userId = CurrentUser.userId();

        if (userId == null) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return notificationRepository
                .findByUserId(userId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMyUnread() {
        UUID userId = CurrentUser.userId();

        if (userId == null) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(UUID id) {
        UUID userId = CurrentUser.userId();

        if (userId == null) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void pushToUser(UUID userId, String title, String content) {

        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(title)
                .content(content)
                .read(false)
                .build();

        notificationRepository.save(notification);

        NotificationResponse response = notificationMapper.toResponse(notification);

        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                response
        );
    }
}