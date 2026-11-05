/*
 * DTO ответа на успешное начало регистрации.
 *
 * Возвращается после POST /api/registrations с HTTP 201 Created.
 * Содержит sessionId, статус регистрации и срок действия сессии.
 *
 * sessionId используется в следующих этапах регистрации.
 * expiresAt относится к сроку сессии, а не к сроку SMS-кода.
 *
 * Не содержит пароль, хеш пароля или код подтверждения.
 * Отделяет публичный ответ API от внутренней JPA-сущности.
 */

package com.example.minibank.registration;

import java.time.Instant;
import java.util.UUID;

public record StartRegistrationResponse(
        UUID sessionId,
        RegistrationStatus status,
        Instant expiresAt
        ) {
}