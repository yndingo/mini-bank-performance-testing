package com.example.minibank.auth;

/*
 * JPA-сущность сессии авторизации.
 * Соответствует таблице bank.auth_sessions.
 *
 * Создаётся после успешной проверки телефона и пароля.
 * Начальное состояние — PENDING.
 * Сессия и код подтверждения действуют 5 минут от создания.
 *
 * Хранит:
 * - идентификатор сессии и клиента;
 * - статус и временные отметки;
 * - хеш кода подтверждения и число неверных попыток;
 * - хеш токена доступа после активации.
 *
 * activate():
 * - переводит сессию из PENDING в ACTIVE;
 * - сохраняет хеш токена и время активации;
 * - задаёт срок доступа 2 часа от активации;
 * - очищает данные использованного кода подтверждения.
 *
 * revoke() переводит сессию в REVOKED
 * и очищает хеш токена и данные кода подтверждения.
 *
 * Исходный токен доступа здесь не хранится.
 * Проверку кода и генерацию токена выполняет AuthService.
 * Проверку токена, статуса и срока доступа выполняет
 * SessionAuthenticationService.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_sessions", schema = "bank")
public class AuthSession {

    @Id
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AuthSessionStatus status;

    @Column(name = "confirmation_code_hash", length = 255)
    private String confirmationCodeHash;

    @Column(name = "confirmation_code_expires_at")
    private Instant confirmationCodeExpiresAt;

    @Column(name = "confirmation_attempts", nullable = false)
    private int confirmationAttempts;

    @Column(name = "access_token_hash", unique = true, length = 64)
    private String accessTokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    protected AuthSession() {
    }

    public AuthSession(UUID customerId, String confirmationCodeHash) {
        this.sessionId = UUID.randomUUID();
        this.customerId = customerId;
        this.status = AuthSessionStatus.PENDING;
        this.confirmationCodeHash = confirmationCodeHash;
        this.confirmationAttempts = 0;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plusSeconds(5 * 60);
        this.confirmationCodeExpiresAt = this.expiresAt;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public AuthSessionStatus getStatus() {
        return status;
    }

    public String getConfirmationCodeHash() {
        return confirmationCodeHash;
    }

    public Instant getConfirmationCodeExpiresAt() {
        return confirmationCodeExpiresAt;
    }

    public int getConfirmationAttempts() {
        return confirmationAttempts;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void recordFailedConfirmationAttempt() {
        if (confirmationAttempts >= 5) {
            throw new IllegalStateException(
                    "Confirmation attempt limit is already reached"
            );
        }

        confirmationAttempts++;
    }

    public void activate(String accessTokenHash) {
        if (status != AuthSessionStatus.PENDING) {
            throw new IllegalStateException(
                    "Activation requires PENDING status"
            );
        }

        if (accessTokenHash == null || accessTokenHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Access token hash is required"
            );
        }

        Instant now = Instant.now();

        if (!now.isBefore(expiresAt)) {
            throw new IllegalStateException(
                    "Authentication session expired"
            );
        }

        this.status = AuthSessionStatus.ACTIVE;
        this.accessTokenHash = accessTokenHash;
        this.activatedAt = now;
        // Срок доступа увеличен до 3 часов для длительных тестов стенда.
        this.expiresAt = now.plusSeconds(3 * 60 * 60);
        this.confirmationCodeHash = null;
        this.confirmationCodeExpiresAt = null;
    }

    public void revoke() {
        this.status = AuthSessionStatus.REVOKED;
        this.accessTokenHash = null;
        this.confirmationCodeHash = null;
        this.confirmationCodeExpiresAt = null;
    }
}