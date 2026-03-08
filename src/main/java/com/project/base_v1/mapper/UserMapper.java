package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.user.UserDetailResponse;
import com.project.base_v1.dto.response.user.UserResponse;
import com.project.base_v1.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    UserDetailResponse toDetail(User user);

}