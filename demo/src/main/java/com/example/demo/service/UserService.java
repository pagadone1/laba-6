package com.example.demo.service;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.PasswordValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        List<String> passwordErrors = PasswordValidator.validate(request.getPassword());
        if (!passwordErrors.isEmpty()) {
            throw new IllegalArgumentException("Ненадёжный пароль: " + String.join("; ", passwordErrors));
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже зарегистрирован");
        }

        String role = normalizeRole(request.getRole());
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Недопустимая роль. Допустимые: ADMIN, MECHANIC, CUSTOMER");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                role.toUpperCase().replace("ROLE_", "")
        );

        return userRepository.save(user);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return "";
        String r = role.trim().toUpperCase();
        return r.startsWith("ROLE_") ? r : "ROLE_" + r;
    }

    private boolean isValidRole(String role) {
        return "ROLE_ADMIN".equals(role) || "ROLE_MECHANIC".equals(role) || "ROLE_CUSTOMER".equals(role);
    }
}
