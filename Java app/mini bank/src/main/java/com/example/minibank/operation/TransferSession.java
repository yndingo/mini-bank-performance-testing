package com.example.minibank.operation;

/*
 * Подготовленный двухэтапный перевод.
 * Соответствует bank.transfer_sessions.
 *
 * Создаётся на этапе prepare со статусом PENDING.
 * Хранит владельца, оба счёта, сумму и срок подтверждения.
 *
 * Самостоятельно баланс не изменяет.
 * complete() сохраняет результат выполненного перевода.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "transfer_sessions", schema = "bank")
public class TransferSession {

    @Id
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "destination_account_id", nullable = false)
    private UUID destinationAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferSessionStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "operation_id", unique = true)
    private UUID operationId;

    @Column(name = "source_balance_after", precision = 19, scale = 2)
    private BigDecimal sourceBalanceAfter;

    protected TransferSession() {
    }

    public TransferSession(
            UUID customerId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount
    ) {
        this.customerId = Objects.requireNonNull(customerId);
        this.sourceAccountId = Objects.requireNonNull(sourceAccountId);
        this.destinationAccountId =
                Objects.requireNonNull(destinationAccountId);

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must differ"
            );
        }

        if (amount == null
                || amount.signum() <= 0
                || amount.compareTo(
                new BigDecimal("1000000.00")
        ) > 0) {
            throw new IllegalArgumentException(
                    "Amount must be between 0.01 and 1000000.00"
            );
        }

        this.amount = amount.setScale(
                2,
                RoundingMode.UNNECESSARY
        );

        this.transactionId = UUID.randomUUID();
        this.currency = "RUB";
        this.status = TransferSessionStatus.PENDING;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        this.expiresAt = this.createdAt.plusSeconds(5 * 60);
    }

    public void complete(
            UUID operationId,
            BigDecimal sourceBalanceAfter,
            Instant completedAt
    ) {
        if (status != TransferSessionStatus.PENDING) {
            throw new IllegalStateException(
                    "Transfer is not awaiting confirmation"
            );
        }

        Objects.requireNonNull(operationId);
        Objects.requireNonNull(sourceBalanceAfter);
        Objects.requireNonNull(completedAt);

        if (sourceBalanceAfter.signum() < 0) {
            throw new IllegalArgumentException(
                    "Balance must not be negative"
            );
        }

        this.operationId = operationId;
        this.sourceBalanceAfter = sourceBalanceAfter.setScale(
                2,
                RoundingMode.UNNECESSARY
        );
        this.completedAt = completedAt.truncatedTo(ChronoUnit.MICROS);
        this.status = TransferSessionStatus.COMPLETED;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public TransferSessionStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public BigDecimal getSourceBalanceAfter() {
        return sourceBalanceAfter;
    }
}