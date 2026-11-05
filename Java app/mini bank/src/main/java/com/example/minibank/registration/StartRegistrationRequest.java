/*
 * DTO входного запроса на начало регистрации.
 *
 * Используется для JSON-тела:
 * POST /api/registrations
 *
 * Содержит ФИО, телефон и исходный пароль.
 * Аннотации валидации задают обязательность полей,
 * максимальную длину и допустимый формат телефона и пароля.
 *
 * Проверка запускается через @Valid в контроллере.
 *
 * Это не JPA-сущность: объект запроса не сохраняется в БД напрямую.
 * Сервис сначала хеширует пароль, затем создаёт RegistrationSession.
 *
 * ВАЖНО: содержит исходный пароль.
 * Не выводить объект целиком в журналы и не возвращать его клиенту.
 */

package com.example.minibank.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StartRegistrationRequest(

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String middleName,

        @NotBlank
        @Pattern(regexp = "^\\+7\\d{10}$")
        String phone,

        @NotBlank
        @Pattern(regexp = "^[\\x21-\\x7E]{8,64}$")
        String password
) {
}