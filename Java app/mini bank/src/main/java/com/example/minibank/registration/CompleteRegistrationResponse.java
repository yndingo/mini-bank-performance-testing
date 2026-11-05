package com.example.minibank.registration;

/*
 * DTO ответа на завершение регистрации.
 *
 * Содержит идентификаторы регистрационной сессии,
 * созданного клиента, счёта и учебной карты.
 * Также возвращает номер счёта, его баланс и данные учебной карты.
 *
 * Не содержит пароль или хеш пароля.
 */

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CompleteRegistrationResponse(
        UUID sessionId,
        RegistrationStatus status,
        UUID customerId,
        UUID accountId,
        String accountNumber,
        String currency,
        BigDecimal balance,
        UUID cardId,
        String cardNumber,
        LocalDate cardExpiresOn
) {
}