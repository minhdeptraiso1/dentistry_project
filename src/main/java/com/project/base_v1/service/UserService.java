package com.project.base_v1.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.base_v1.dto.request.user.ChangePasswordRequest;
import com.project.base_v1.dto.request.user.CreateUserRequest;
import com.project.base_v1.dto.request.user.UpdateUserRequest;
import com.project.base_v1.dto.request.user.UserSearchRequest;
import com.project.base_v1.dto.response.patient.ActiveDoctorResponse;
import com.project.base_v1.dto.response.user.UserDetailResponse;
import com.project.base_v1.dto.response.user.UserResponse;

public interface UserService {
    Page<UserResponse> searchUsers(
            UserSearchRequest request,
            Pageable pageable
    );

    void deleteUserById(UUID userId);

    UserResponse getCurrentUser();

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    UserDetailResponse getUserById(UUID userId);

    UserDetailResponse getUserByPatientId(UUID patientId);

    void changePassword(UUID id, ChangePasswordRequest request);

    List<ActiveDoctorResponse> getActiveDoctorsForPatient();
}
