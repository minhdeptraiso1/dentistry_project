package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.notification.NotificationResponse;
import com.project.base_v1.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}