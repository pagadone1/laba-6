package com.example.demo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Проверка надёжности пароля: длина, спецсимволы, цифры, буквы.
 */
public final class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern UPPER = Pattern.compile("[A-ZА-ЯЁ]");
    private static final Pattern LOWER = Pattern.compile("[a-zа-яё]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]");

    private PasswordValidator() {
    }

    public static List<String> validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isBlank()) {
            errors.add("Пароль не может быть пустым");
            return errors;
        }

        if (password.length() < MIN_LENGTH) {
            errors.add("Пароль должен содержать не менее " + MIN_LENGTH + " символов");
        }

        if (password.length() > MAX_LENGTH) {
            errors.add("Пароль должен содержать не более " + MAX_LENGTH + " символов");
        }

        if (!DIGIT.matcher(password).find()) {
            errors.add("Пароль должен содержать хотя бы одну цифру");
        }

        if (!UPPER.matcher(password).find()) {
            errors.add("Пароль должен содержать хотя бы одну заглавную букву");
        }

        if (!LOWER.matcher(password).find()) {
            errors.add("Пароль должен содержать хотя бы одну строчную букву");
        }

        if (!SPECIAL.matcher(password).find()) {
            errors.add("Пароль должен содержать хотя бы один спецсимвол (!@#$%^&* и т.д.)");
        }

        return errors;
    }

    public static boolean isValid(String password) {
        return validate(password).isEmpty();
    }
}
