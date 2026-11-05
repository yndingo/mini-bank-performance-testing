package com.example.minibank.operation;

/*
 * Результат подготовки перевода.
 *
 * Деньги ещё не перемещались.
 * transactionId используется для подтверждения.
 */

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PrepareTransferResponse(
        UUID transactionId,
        TransferSessionStatus status,
        UUID sourceAccountId,
        String destinationAccountNumber,
        BigDecimal amount,
        String currency,
        Instant expiresAt
) {
}