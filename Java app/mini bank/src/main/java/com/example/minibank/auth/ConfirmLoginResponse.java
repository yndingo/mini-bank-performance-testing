package com.example.minibank.auth;

/*
 * DTO успешного завершения входа.
 *
 * Содержит идентификатор сессии, статус ACTIVE,
 * токен доступа и время его окончания.
 *
 * accessToken — секрет.
 * Не публиковать и не выводить в журналы.
 * В БД сохраняется только его хеш.
 */

import java.time.Instant;
import java.util.UUID;

public record ConfirmLoginResponse(
        UUID sessionId,
        AuthSessionStatus status,
        String accessToken,
        String tokenType,
        Instant expiresAt
) {
}