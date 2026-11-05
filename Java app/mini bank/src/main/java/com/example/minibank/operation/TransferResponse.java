package com.example.minibank.operation;

/*
 * DTO успешного перевода.
 *
 * Возвращает операцию, сумму и остаток отправителя.
 * Баланс получателя в ответ не включается.
 */

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID operationId,
        OperationType operationType,
        UUID sourceAccountId,
        String destinationAccountNumber,
        BigDecimal amount,
        String currency,
        BigDecimal sourceBalance,
        Instant createdAt
) {
}