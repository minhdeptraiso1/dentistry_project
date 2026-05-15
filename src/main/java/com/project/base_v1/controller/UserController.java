package com.project.base_v1.controller;

import com.project.base_v1.dto.request.user.*;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.user.UserDetailResponse;
import com.project.base_v1.dto.response.user.UserResponse;
import com.project.base_v1.service.PasswordService;
import com.project.base_v1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(
        name = "User",
        description = "APIs for user management"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    PasswordService passwordService;

    // ===================== ME =====================
    @Operation(
            summary = "Get current logged-in user",
            description = "Return information of the currently authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ApiResponseSever<UserResponse> me() {
        return ApiResponseSever.ok(userService.getCurrentUser());
    }

    // ===================== SEARCH =====================
    @Operation(
            summary = "Search users",
            description = """
                        Search users by keyword, role and status.
                    
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search success"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponseSever<Page<UserResponse>> search(
            @Parameter(
                    description = "Search by username or email",
                    example = "john"
            )
            @ParameterObject UserSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(userService.searchUsers(request, pageable));
    }

    // ===================== CREATE =====================
    @Operation(
            summary = "Create new user",
            description = "Create a new user account (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponseSever<UserResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User creation payload",
                    required = true
            )
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ApiResponseSever.ok(userService.createUser(request));
    }

    // ===================== UPDATE =====================
    @Operation(
            summary = "Update user",
            description = "Update user information by id (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponseSever<UserResponse> update(
            @Parameter(
                    description = "User ID",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User update payload"
            )
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ApiResponseSever.ok(userService.updateUser(id, request));
    }

    // ===================== DELETE =====================
    @Operation(
            summary = "Delete user",
            description = "Soft delete user by id (ADMIN only)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponseSever<Void> delete(
            @Parameter(
                    description = "User ID to delete",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id
    ) {
        userService.deleteUserById(id);
        return ApiResponseSever.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponseSever<UserDetailResponse> getById(
            @Parameter(
                    description = "User ID",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id) {
        return ApiResponseSever.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "Get user by patient id",
            description = "Lookup user account by patientId (ADMIN/CASHIER)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    @GetMapping("/by-patient/{patientId}")
    public ApiResponseSever<UserDetailResponse> getByPatientId(
            @PathVariable UUID patientId
    ) {
        return ApiResponseSever.ok(userService.getUserByPatientId(patientId));
    }

    @PutMapping("/{id}/password")
    public ApiResponseSever<Void> changePassword(
            @PathVariable UUID id,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        userService.changePassword(id, request);
        return ApiResponseSever.ok(null);
    }

    @PutMapping("/{id}/admin-reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponseSever<String> adminResetPassword(
            @PathVariable UUID id,
            @RequestBody @Valid AdminResetPasswordRequest request
    ) {
        passwordService.adminResetPassword(id, request);
        return ApiResponseSever.ok("Admin đổi mật khẩu tài khoản thành công.");
    }
}

