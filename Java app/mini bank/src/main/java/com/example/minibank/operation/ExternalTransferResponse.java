package com.example.minibank.operation;

/*
 * Ответ подготовки и подтверждения внешнего перевода.
 *
 * В состоянии PENDING поля operationId,
 * sourceBalance и completedAt отсутствуют по смыслу
 * и имеют значение null.
 *
 * COMPLETED означает завершение учебной имитации,
 * а не подтверждение от настоящего внешнего банка.
 */

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExternalTransferResponse(
        UUID transactionId,
        TransferSessionStatus status,
        UUID sourceAccountId,
        String recipientName,
        String recipientAccount,
        String recipientBankCode,
        BigDecimal amount,
        String currency,
        Instant expiresAt,
        UUID operationId,
        BigDecimal sourceBalance,
        Instant completedAt,
        boolean simulated
) {

    public static ExternalTransferResponse from(
            ExternalTransferSession session
    ) {
        return new ExternalTransferResponse(
                session.getTransactionId(),
                session.getStatus(),
                session.getSourceAccountId(),
                session.getRecipientName(),
                session.getRecipientAccount(),
                session.getRecipientBankCode(),
                session.getAmount(),
                session.getCurrency(),
                session.getExpiresAt(),
                session.getOperationId(),
                session.getSourceBalanceAfter(),
                session.getCompletedAt(),
                true
        );
    }
}