package com.example.demo.entity;

/**
 * Статус сессии пользователя.
 */
public enum SessionStatus {
    /** Активная сессия, refresh-токен можно использовать */
    ACTIVE,

    /** Сессия отозвана (логаут или одноразовое использование refresh) */
    REVOKED
}
