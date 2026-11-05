package com.example.minibank.operation;

/*
 * JPA-сущность результата успешного запроса перевода.
 * Соответствует bank.transfer_requests.
 *
 * Хранит:
 * - клиента и ключ идемпотентности;
 * - исходные параметры перевода;
 * - идентификатор выполненной операции;
 * - остаток отправителя из первоначального ответа.
 *
 * Сама перевод не выполняет.
 * Сохраняется сервисом в одной транзакции с переводом.
 */

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "transfer_requests", schema = "bank")
public class TransferRequestRecord {

    @EmbeddedId
    private TransferRequestId id;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(
            name = "destination_account_number",
            nullable = false,
            length = 40
    )
    private String destinationAccountNumber;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "operation_id", nullable = false, unique = true)
    private UUID operationId;

    @Column(
            name = "source_balance_after",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal sourceBalanceAfter;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransferRequestRecord() {
    }

    public TransferRequestRecord(
            UUID customerId,
            UUID idempotencyKey,
            TransferResponse response
    ) {
        Objects.requireNonNull(response);

        this.id = new TransferRequestId(
                customerId,
                idempotencyKey
        );

        this.sourceAccountId = response.sourceAccountId();
        this.destinationAccountNumber =
                response.destinationAccountNumber();
        this.amount = response.amount();
        this.operationId = response.operationId();
        this.sourceBalanceAfter = response.sourceBalance();
        this.createdAt = response.createdAt();
    }

    public boolean matches(
            TransferRequest request,
            BigDecimal normalizedAmount
    ) {
        return sourceAccountId.equals(request.sourceAccountId())
                && destinationAccountNumber.equals(
                request.destinationAccountNumber()
        )
                && amount.compareTo(normalizedAmount) == 0;
    }

    public TransferResponse toResponse() {
        return new TransferResponse(
                operationId,
                OperationType.TRANSFER,
                sourceAccountId,
                destinationAccountNumber,
                amount,
                "RUB",
                sourceBalanceAfter,
                createdAt
        );
    }
}