package com.example.minibank.operation;

/*
 * Репозиторий внешних переводов.
 *
 * При подтверждении загружает сессию владельца
 * с блокировкой внутри транзакции.
 */

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ExternalTransferSessionRepository
        extends JpaRepository<ExternalTransferSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select session
            from ExternalTransferSession session
            where session.transactionId = :transactionId
              and session.customerId = :customerId
            """)
    Optional<ExternalTransferSession> findOwnedForUpdate(
            @Param("transactionId") UUID transactionId,
            @Param("customerId") UUID customerId
    );
}