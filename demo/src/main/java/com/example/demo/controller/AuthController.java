package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.TokenPairResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.service.TokenPairService;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final TokenPairService tokenPairService;

    public AuthController(UserService userService, TokenPairService tokenPairService) {
        this.userService = userService;
        this.tokenPairService = tokenPairService;
    }

    /**
     * Регистрация нового пользователя.
     * Доступна без аутентификации.
     * Пароль проверяется на надёжность (длина, спецсимволы, цифры, буквы).
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return new UserResponse(user);
    }

    /**
     * Логин: возвращает пару access и refresh токенов.
     */
    @PostMapping("/auth/login")
    public TokenPairResponse login(@Valid @RequestBody LoginRequest request) {
        return tokenPairService.login(request.getUsername(), request.getPassword());
    }

    /**
     * Обновление пары токенов по refresh-токену.
     * Refresh-токен одноразовый — после использования становится недействительным.
     */
    @PostMapping("/auth/refresh")
    public TokenPairResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return tokenPairService.refresh(request.getRefreshToken());
    }
}
