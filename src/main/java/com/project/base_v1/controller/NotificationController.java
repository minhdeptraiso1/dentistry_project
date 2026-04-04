package com.project.base_v1.controller;

import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.notification.NotificationResponse;
import com.project.base_v1.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notification", description = "APIs for notifications")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ApiResponseSever<List<NotificationResponse>> getMyNotifications() {
        return ApiResponseSever.ok(notificationService.getMyNotifications());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my/unread-count")
    public ApiResponseSever<Long> countMyUnread() {
        return ApiResponseSever.ok(notificationService.countMyUnread());
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/my/{id}/read")
    public ApiResponseSever<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ApiResponseSever.ok(null);
    }
}