package com.example.minibank.auth;

/*
 * DTO первого этапа входа.
 * Используется в POST /api/auth/login.
 *
 * Принимает телефон и исходный пароль.
 * Правила формата соответствуют нашей текущей регистрации.
 *
 * Содержит секрет: не выводить объект целиком в журналы.
 * Не сохраняется в БД как сущность.
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(

        @NotBlank
        @Pattern(regexp = "^\\+7\\d{10}$")
        String phone,

        @NotBlank
        @Pattern(regexp = "^[\\x21-\\x7E]{8,64}$")
        String password
) {
}