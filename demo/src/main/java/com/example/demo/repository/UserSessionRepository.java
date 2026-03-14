package com.example.demo.repository;

import com.example.demo.entity.SessionStatus;
import com.example.demo.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshTokenIdAndStatus(String refreshTokenId, SessionStatus status);

    boolean existsByRefreshTokenIdAndStatus(String refreshTokenId, SessionStatus status);

    void deleteByExpiresAtBefore(Instant instant);
}
