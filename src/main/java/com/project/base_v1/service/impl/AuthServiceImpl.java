package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.auth.LoginRequest;
import com.project.base_v1.dto.response.auth.AuthResponse;
import com.project.base_v1.entity.TokenSession;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.AuditAction;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.TokenSessionRepository;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.security.JwtTokenProvider;
import com.project.base_v1.security.LoginRateLimiter;
import com.project.base_v1.security.TokenBlacklistService;
import com.project.base_v1.service.AuditLogService;
import com.project.base_v1.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    AuthenticationManager authenticationManager;
    JwtTokenProvider jwtTokenProvider;
    UserRepository userRepository;
    TokenSessionRepository tokenSessionRepository;
    TokenBlacklistService tokenBlacklistService;
    LoginRateLimiter loginRateLimiter;
    AuditLogService auditLogService;

    @Override
    public AuthResponse login(LoginRequest request) {

        String rateKey = "login:" + request.username();

        loginRateLimiter.check(rateKey);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        } catch (DisabledException ex) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        loginRateLimiter.reset(rateKey);

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_CREDENTIALS)
                );

        TokenSession session = TokenSession.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .revoked(false)
                .expiredAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        String refreshToken =
                jwtTokenProvider.generateRefreshToken(session.getId());

        session.setRefreshToken(refreshToken);
        tokenSessionRepository.save(session);

        String accessToken =
                jwtTokenProvider.generateAccessToken(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().name()
                );

        auditLogService.log(user.getId(), AuditAction.LOGIN.name());

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {

        TokenSession session =
                tokenSessionRepository.findByRefreshToken(refreshToken)
                        .orElseThrow(() ->
                                new BusinessException(ErrorCode.TOKEN_EXPIRED)
                        );

        if (session.isRevoked()) {
            throw new BusinessException(ErrorCode.TOKEN_REVOKED);
        }

        if (session.getExpiredAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.SYSTEM_ERROR)
                );

        String newAccessToken =
                jwtTokenProvider.generateAccessToken(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().name()
                );

        return new AuthResponse(newAccessToken, refreshToken);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {


        tokenBlacklistService.revoke(
                accessToken,
                Duration.ofMinutes(15)
        );


        tokenSessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.setRevoked(true);
                    tokenSessionRepository.save(session);
                });

        UUID userId = jwtTokenProvider.getUserId(accessToken);

        auditLogService.log(userId, AuditAction.LOGOUT.name());
    }
}


