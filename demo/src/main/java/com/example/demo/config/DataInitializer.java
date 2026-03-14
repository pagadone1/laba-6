package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin").ifPresentOrElse(
                user -> {
                    if (!"ADMIN".equals(user.getRole())) {
                        user.setRole("ADMIN");
                        userRepository.save(user);
                    }
                },
                () -> {
                    User admin = new User(
                            "admin",
                            passwordEncoder.encode("Admin123!"),
                            "admin@carservice.ru",
                            "ADMIN"
                    );
                    userRepository.save(admin);
                }
        );
    }
}
