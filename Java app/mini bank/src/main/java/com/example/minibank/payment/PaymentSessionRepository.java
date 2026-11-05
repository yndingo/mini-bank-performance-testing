package com.example.minibank.payment;

/*
 * Репозиторий подготовленных платежей.
 *
 * При подтверждении загружает платёж владельца
 * с блокировкой внутри транзакции сервиса.
 */

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentSessionRepository
        extends JpaRepository<PaymentSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select payment
            from PaymentSession payment
            where payment.transactionId = :transactionId
              and payment.customerId = :customerId
            """)
    Optional<PaymentSession> findOwnedForUpdate(
            @Param("transactionId") UUID transactionId,
            @Param("customerId") UUID customerId
    );
}