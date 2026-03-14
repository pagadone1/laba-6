package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Запрос на обновление токенов.
 */
public class RefreshRequest {

    @NotBlank(message = "Refresh-токен обязателен")
    private String refreshToken;

    public RefreshRequest() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
