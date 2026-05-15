package com.project.base_v1.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.base_v1.dto.request.user.ChangePasswordRequest;
import com.project.base_v1.dto.request.user.CreateUserRequest;
import com.project.base_v1.dto.request.user.UpdateUserRequest;
import com.project.base_v1.dto.request.user.UserSearchRequest;
import com.project.base_v1.dto.response.patient.ActiveDoctorResponse;
import com.project.base_v1.dto.response.user.UserDetailResponse;
import com.project.base_v1.dto.response.user.UserResponse;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.UserRole;
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
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .img(request.img())
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

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.img() != null) {
            user.setImg(request.img());
        }
        return userMapper.toResponse(user);
    }

    @Override
    public UserDetailResponse getUserById(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        return userMapper.toDetail(user);
    }

    @Override
    public UserDetailResponse getUserByPatientId(UUID patientId) {

        User user = userRepository.findByPatient_Id(patientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toDetail(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    @Override
    public List<ActiveDoctorResponse> getActiveDoctorsForPatient() {
        return userRepository.findByRoleAndEnabledTrueOrderByNameAsc(UserRole.DOCTOR)
                .stream()
                .map(user -> new ActiveDoctorResponse(
                        user.getId(),
                        user.getName(),
                        user.getImg()
                ))
                .toList();
    }
}

