package com.project.base_v1.repository;

import com.project.base_v1.entity.TokenSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenSessionRepository
        extends JpaRepository<TokenSession, UUID> {

    Optional<TokenSession> findByRefreshToken(String refreshToken);
}
