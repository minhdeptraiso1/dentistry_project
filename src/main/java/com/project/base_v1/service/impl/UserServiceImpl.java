package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.user.CreateUserRequest;
import com.project.base_v1.dto.request.user.UpdateUserRequest;
import com.project.base_v1.dto.request.user.UserSearchRequest;
import com.project.base_v1.dto.response.user.UserResponse;
import com.project.base_v1.entity.User;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.UserMapper;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.repository.spec.UserSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getCurrentUser() {
        String username = CurrentUser.username();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        return userMapper.toResponse(user);
    }

    @Override
    public Page<UserResponse> searchUsers(
            UserSearchRequest request,
            Pageable pageable
    ) {
        Specification<User> spec = Specification.allOf(
                UserSpecification.hasKeyword(request.keyword()),
                UserSpecification.hasRole(request.role()),
                UserSpecification.isEnabled(request.enabled())
        );

        return userRepository.findAll(spec, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setDeletedAt(Instant.now());
        user.setDeletedBy(CurrentUser.username());
        userRepository.save(user);
    }


    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .enabled(true)
                .build();

        userRepository.save(user);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.role() != null) {
            user.setRole(request.role());
        }

        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        return userMapper.toResponse(user);
    }
}

