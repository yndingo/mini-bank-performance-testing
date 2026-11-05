package com.example.minibank.operation;

/*
 * Составной идентификатор записи запроса перевода:
 * customerId + idempotencyKey.
 *
 * Соответствует первичному ключу bank.transfer_requests.
 * equals() и hashCode() сравнивают обе части.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class TransferRequestId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "idempotency_key", nullable = false)
    private UUID idempotencyKey;

    public TransferRequestId() {
    }

    public TransferRequestId(
            UUID customerId,
            UUID idempotencyKey
    ) {
        this.customerId = Objects.requireNonNull(customerId);
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof TransferRequestId other)) {
            return false;
        }

        return Objects.equals(customerId, other.customerId)
                && Objects.equals(
                idempotencyKey,
                other.idempotencyKey
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, idempotencyKey);
    }
}