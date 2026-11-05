/*
 * DTO ответа на успешное подтверждение телефона.
 *
 * Возвращается после:
 * POST /api/registrations/{sessionId}/confirm-phone
 *
 * Содержит sessionId и статус PHONE_CONFIRMED.
 * Успешный HTTP-статус — 200 OK.
 *
 * Подтверждение телефона ещё не завершает регистрацию:
 * создание клиента, счёта и карты выполняется следующим этапом.
 *
 * Не содержит код подтверждения или его хеш.
 */

package com.example.minibank.registration;

import java.util.UUID;

public record ConfirmPhoneResponse(
        UUID sessionId,
        RegistrationStatus status
) {
}