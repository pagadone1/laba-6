package com.example.demo.security;

import com.example.demo.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Провайдер JWT-токенов с раздельной логикой для access и refresh токенов.
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "sub";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String CLAIM_SESSION_ID = "sessionId";

    private final SecretKey secretKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access.expiration-ms}") long accessExpirationMs,
            @Value("${jwt.refresh.expiration-ms}") long refreshExpirationMs
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("jwt.secret должен быть не менее 32 символов");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /**
     * Генерирует access-токен для пользователя.
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessExpirationMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole())
                .claim(CLAIM_TOKEN_TYPE, JwtTokenType.ACCESS.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Генерирует refresh-токен и возвращает пару (токен, sessionId для хранения в БД).
     */
    public RefreshTokenData generateRefreshToken(User user) {
        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshExpirationMs);

        String token = Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_TOKEN_TYPE, JwtTokenType.REFRESH.name())
                .claim(CLAIM_SESSION_ID, sessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();

        return new RefreshTokenData(token, sessionId, expiry);
    }

    /**
     * Валидирует access-токен и извлекает username.
     */
    public String validateAccessToken(String token) {
        Claims claims = parseAndValidate(token, JwtTokenType.ACCESS);
        return claims.getSubject();
    }

    /**
     * Валидирует refresh-токен и возвращает данные для обновления.
     */
    public RefreshValidationResult validateRefreshToken(String token) {
        Claims claims = parseAndValidate(token, JwtTokenType.REFRESH);
        String sessionId = claims.get(CLAIM_SESSION_ID, String.class);
        Long userId = claims.get(CLAIM_USER_ID, Long.class);
        String username = claims.getSubject();
        if (sessionId == null || userId == null || username == null) {
            throw new JwtException("Refresh-токен содержит неполные данные");
        }
        return new RefreshValidationResult(userId, username, sessionId);
    }

    private Claims parseAndValidate(String token, JwtTokenType expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            if (!expectedType.name().equals(tokenType)) {
                throw new JwtException("Неверный тип токена: ожидается " + expectedType);
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new JwtException("Токен истёк");
        } catch (JwtException e) {
            throw new JwtException("Недействительный токен: " + e.getMessage());
        }
    }

    public record RefreshTokenData(String token, String sessionId, Instant expiresAt) {}

    public record RefreshValidationResult(Long userId, String username, String sessionId) {}
}
