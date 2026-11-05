package com.example.minibank.operation;

/*
 * DTO результата учебного пополнения.
 *
 * Возвращает идентификаторы операции и счёта,
 * зачисленную сумму, новый баланс и время операции.
 */

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DemoDepositResponse(
        UUID operationId,
        OperationType operationType,
        UUID accountId,
        BigDecimal amount,
        String currency,
        BigDecimal balance,
        Instant createdAt
) {
}