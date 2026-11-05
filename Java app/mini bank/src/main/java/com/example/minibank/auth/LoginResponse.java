package com.example.minibank.auth;

/*
 * DTO ответа первого этапа входа.
 *
 * Возвращает идентификатор ожидающей сессии,
 * статус PENDING и срок её действия.
 *
 * Это ещё не разрешение доступа к банковским операциям.
 * Токен будет выдан после подтверждения кода.
 */

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        UUID sessionId,
        AuthSessionStatus status,
        Instant expiresAt
) {
}