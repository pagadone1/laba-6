package com.example.demo.service;

import com.example.demo.dto.TokenPairResponse;
import com.example.demo.entity.SessionStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.UserSession;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSessionRepository;
import com.example.demo.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * Сервис работы с парами токенов (access + refresh).
 */
@Service
public class TokenPairService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenPairService(UserRepository userRepository,
                            UserSessionRepository sessionRepository,
                            PasswordEncoder passwordEncoder,
                            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Логин: проверка учётных данных и выдача пары токенов.
     */
    @Transactional
    public TokenPairResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль"));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Учётная запись отключена");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Неверный логин или пароль");
        }

        return issueTokenPair(user);
    }

    /**
     * Обновление токенов по refresh-токену.
     * Refresh-токен одноразовый: после использования сессия помечается REVOKED.
     */
    @Transactional
    public TokenPairResponse refresh(String refreshToken) {
        try {
            JwtTokenProvider.RefreshValidationResult validation = jwtTokenProvider.validateRefreshToken(refreshToken);
            String sessionId = validation.sessionId();
            Long userId = validation.userId();
            String username = validation.username();

            UserSession session = sessionRepository.findByRefreshTokenIdAndStatus(sessionId, SessionStatus.ACTIVE)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Сессия не найдена или уже использована"));

            if (session.getExpiresAt().isBefore(Instant.now())) {
                session.setStatus(SessionStatus.REVOKED);
                sessionRepository.save(session);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh-токен истёк");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));

            if (!user.getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Недействительный refresh-токен");
            }

            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.save(session);

            return issueTokenPair(user);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Недействительный refresh-токен: " + e.getMessage());
        }
    }

    private TokenPairResponse issueTokenPair(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        JwtTokenProvider.RefreshTokenData refreshData = jwtTokenProvider.generateRefreshToken(user);

        UserSession session = new UserSession(
                user,
                refreshData.sessionId(),
                Instant.now(),
                refreshData.expiresAt()
        );
        sessionRepository.save(session);

        return new TokenPairResponse(accessToken, refreshData.token());
    }
}
