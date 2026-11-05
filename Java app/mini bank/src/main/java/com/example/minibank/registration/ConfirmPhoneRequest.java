/*
 * DTO входного запроса на подтверждение телефона.
 *
 * Используется для JSON-тела:
 * POST /api/registrations/{sessionId}/confirm-phone
 *
 * Содержит code — строку ровно из шести цифр.
 * Строковый тип позволяет сохранить ведущие нули.
 *
 * sessionId передаётся отдельно в пути запроса.
 *
 * Аннотации проверяют только формат кода.
 * Правильность кода, сроки и число попыток проверяет сервис.
 *
 * ВАЖНО: не выводить этот объект целиком в журналы.
 */

package com.example.minibank.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmPhoneRequest(

        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$")
        String code
) {
}