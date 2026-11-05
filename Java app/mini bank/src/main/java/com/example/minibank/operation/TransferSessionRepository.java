package com.example.minibank.operation;

/*
 * Репозиторий двухэтапных переводов.
 *
 * findOwnedForUpdate():
 * загружает перевод текущего клиента с блокировкой.
 *
 * Используется при подтверждении внутри транзакции,
 * чтобы последовательно обрабатывать повторные подтверждения.
 */

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransferSessionRepository
        extends JpaRepository<TransferSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select session
            from TransferSession session
            where session.transactionId = :transactionId
              and session.customerId = :customerId
            """)
    Optional<TransferSession> findOwnedForUpdate(
            @Param("transactionId") UUID transactionId,
            @Param("customerId") UUID customerId
    );
}