package com.example.minibank.payment;

/*
 * Ответ обоих этапов платежа.
 *
 * При PENDING поля operationId, sourceBalance
 * и completedAt имеют значение null.
 *
 * simulated = true: реальная платёжная система
 * в учебном проекте не вызывается.
 */

import com.example.minibank.operation.TransferSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID transactionId,
        TransferSessionStatus status,
        UUID templateId,
        UUID sourceAccountId,
        String recipientName,
        String recipientAccount,
        String recipientBankCode,
        String paymentPurpose,
        BigDecimal amount,
        String currency,
        Instant expiresAt,
        UUID operationId,
        BigDecimal sourceBalance,
        Instant completedAt,
        boolean simulated
) {

    public static PaymentResponse from(PaymentSession session) {
        return new PaymentResponse(
                session.getTransactionId(),
                session.getStatus(),
                session.getTemplateId(),
                session.getSourceAccountId(),
                session.getRecipientName(),
                session.getRecipientAccount(),
                session.getRecipientBankCode(),
                session.getPaymentPurpose(),
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