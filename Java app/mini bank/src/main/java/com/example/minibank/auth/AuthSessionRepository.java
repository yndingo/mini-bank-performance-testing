package com.example.minibank.auth;

/*
 * Репозиторий сессий авторизации.
 *
 * JpaRepository предоставляет стандартные операции
 * сохранения и поиска по sessionId.
 *
 * findBySessionIdForUpdate() загружает сессию с блокировкой
 * для последовательной обработки подтверждений.
 * Должен вызываться внутри транзакции сервиса.
 *
 * findByAccessTokenHash() находит сессию по хешу токена.
 * Сам по себе найденный объект ещё не означает разрешение доступа:
 * сервис обязан проверить статус ACTIVE и срок expiresAt.
 */

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository
        extends JpaRepository<AuthSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select authSession
            from AuthSession authSession
            where authSession.sessionId = :sessionId
            """)
    Optional<AuthSession> findBySessionIdForUpdate(
            @Param("sessionId") UUID sessionId
    );

    Optional<AuthSession> findByAccessTokenHash(String accessTokenHash);
}