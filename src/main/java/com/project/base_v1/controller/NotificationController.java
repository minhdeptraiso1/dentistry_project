package com.project.base_v1.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.notification.NotificationResponse;
import com.project.base_v1.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Tag(name = "Notification", description = "APIs for notifications")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @Operation(
            summary = "Get my notifications",
            description = """
                        Lấy danh sách thông báo của user (phân trang).
                        <br/><b>Roles:</b> AUTHENTICATED
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ApiResponseSever<Page<NotificationResponse>> getMyNotifications(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(notificationService.getMyNotifications(pageable));
    }

    @Operation(
            summary = "Count unread notifications",
            description = """
                        Đếm số thông báo chưa đọc.
                        <br/><b>Roles:</b> AUTHENTICATED
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my/unread-count")
    public ApiResponseSever<Long> countMyUnread() {
        return ApiResponseSever.ok(notificationService.countMyUnread());
    }

    @Operation(
            summary = "Mark notification as read",
            description = """
                        Đánh dấu thông báo đã đọc.
                        <br/><b>Roles:</b> AUTHENTICATED
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/my/{id}/read")
    public ApiResponseSever<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ApiResponseSever.ok(null);
    }
}