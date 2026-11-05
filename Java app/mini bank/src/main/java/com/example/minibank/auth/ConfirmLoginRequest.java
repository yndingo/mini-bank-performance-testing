package com.example.minibank.auth;

/*
 * DTO второго этапа входа.
 *
 * Принимает шестизначный код.
 * sessionId передаётся отдельно в пути HTTP-запроса.
 *
 * Не выводить объект целиком в журналы.
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmLoginRequest(

        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$")
        String code
) {
}