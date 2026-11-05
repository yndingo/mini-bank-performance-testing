package com.example.minibank.operation;

/*
 * Результат подтверждения перевода.
 *
 * transactionId связывает этапы.
 * operationId связывает перевод с историей операций.
 *
 * При повторном подтверждении возвращается
 * сохранённый результат без нового списания.
 */

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ConfirmTransferResponse(
        UUID transactionId,
        TransferSessionStatus status,
        UUID operationId,
        BigDecimal amount,
        String currency,
        BigDecimal sourceBalance,
        Instant completedAt
) {

    public static ConfirmTransferResponse from(
            TransferSession session
    ) {
        return new ConfirmTransferResponse(
                session.getTransactionId(),
                session.getStatus(),
                session.getOperationId(),
                session.getAmount(),
                session.getCurrency(),
                session.getSourceBalanceAfter(),
                session.getCompletedAt()
        );
    }
}