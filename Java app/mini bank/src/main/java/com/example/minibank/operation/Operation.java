package com.example.minibank.operation;

/*
 * JPA-сущность успешно выполненной операции.
 * Соответствует таблице bank.operations.
 *
 * Для DEMO_DEPOSIT:
 * - счёт списания отсутствует;
 * - указан счёт зачисления.
 *
 * Для TRANSFER:
 * - указаны оба счёта.
 *
 * Сама сущность не изменяет баланс.
 * Изменение счёта и сохранение операции
 * выполняются сервисом в одной транзакции.
 *
 * Пока предоставлена фабрика только для пополнения.
 * Создание переводов добавим отдельно.
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
import java.util.UUID;

import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "operations", schema = "bank")
public class Operation {

    @Id
    @Column(name = "operation_id", nullable = false)
    private UUID operationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    private OperationType operationType;

    @Column(name = "source_account_id")
    private UUID sourceAccountId;

    @Column(name = "destination_account_id")
    private UUID destinationAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Operation() {
    }

    public static Operation demoDeposit(
            UUID destinationAccountId,
            BigDecimal amount
    ) {
        if (destinationAccountId == null) {
            throw new IllegalArgumentException(
                    "Destination account is required"
            );
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        BigDecimal normalizedAmount = amount.setScale(
                2,
                RoundingMode.UNNECESSARY
        );

        if (normalizedAmount.precision() > 19) {
            throw new IllegalArgumentException(
                    "Amount exceeds database precision"
            );
        }

        Operation operation = new Operation();

        operation.operationId = UUID.randomUUID();
        operation.operationType = OperationType.DEMO_DEPOSIT;
        operation.sourceAccountId = null;
        operation.destinationAccountId = destinationAccountId;
        operation.amount = normalizedAmount;
        operation.currency = "RUB";
        operation.createdAt = Instant.now().truncatedTo(
                ChronoUnit.MICROS
        );

        return operation;
    }

    public static Operation transfer(
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount
    ) {
        if (sourceAccountId == null || destinationAccountId == null) {
            throw new IllegalArgumentException(
                    "Both accounts are required"
            );
        }

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must differ"
            );
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        BigDecimal normalizedAmount = amount.setScale(
                2,
                RoundingMode.UNNECESSARY
        );

        if (normalizedAmount.precision() > 19) {
            throw new IllegalArgumentException(
                    "Amount exceeds database precision"
            );
        }

        Operation operation = new Operation();

        operation.operationId = UUID.randomUUID();
        operation.operationType = OperationType.TRANSFER;
        operation.sourceAccountId = sourceAccountId;
        operation.destinationAccountId = destinationAccountId;
        operation.amount = normalizedAmount;
        operation.currency = "RUB";
        operation.createdAt = Instant.now().truncatedTo(
                ChronoUnit.MICROS
        );

        return operation;
    }


    public static Operation externalTransfer(
            UUID sourceAccountId,
            BigDecimal amount
    ) {
        if (sourceAccountId == null) {
            throw new IllegalArgumentException(
                    "Source account is required"
            );
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        BigDecimal normalizedAmount = amount.setScale(
                2,
                RoundingMode.UNNECESSARY
        );

        if (normalizedAmount.precision() > 19) {
            throw new IllegalArgumentException(
                    "Amount exceeds database precision"
            );
        }

        Operation operation = new Operation();

        operation.operationId = UUID.randomUUID();
        operation.operationType = OperationType.EXTERNAL_TRANSFER;
        operation.sourceAccountId = sourceAccountId;
        operation.destinationAccountId = null;
        operation.amount = normalizedAmount;
        operation.currency = "RUB";
        operation.createdAt = Instant.now().truncatedTo(
                ChronoUnit.MICROS
        );

        return operation;
    }


    public static Operation templatePayment(
            UUID sourceAccountId,
            BigDecimal amount
    ) {
        Operation operation = externalTransfer(
                sourceAccountId,
                amount
        );

        operation.operationType = OperationType.TEMPLATE_PAYMENT;

        return operation;
    }


    public UUID getOperationId() {
        return operationId;
    }

    public OperationType getOperationType() {
        return operationType;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}