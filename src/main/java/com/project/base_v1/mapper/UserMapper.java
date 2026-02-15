package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.user.UserResponse;
import com.project.base_v1.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRole()
        );
    }
}
