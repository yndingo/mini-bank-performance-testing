/*
 * JPA-сущность регистрационной сессии.
 * Соответствует таблице bank.registration_sessions в PostgreSQL.
 *
 * Хранит временные данные регистрации:
 * - sessionId для связи её этапов;
 * - ФИО и телефон;
 * - хеш пароля;
 * - текущий статус;
 * - время создания и срок действия сессии;
 * - хеш кода подтверждения и срок его действия;
 * - число неверных попыток ввода кода.
 *
 * При создании задаёт UUID, статус PENDING и срок сессии 15 минут.
 * initializeConfirmationCode() сохраняет хеш кода со сроком 5 минут.
 * recordFailedConfirmationAttempt() увеличивает счётчик ошибок.
 * markPhoneConfirmed() меняет статус и удаляет данные использованного кода.
 *
 * Сущность не принимает HTTP-запросы и сама не отправляет SMS.
 * Проверки срока, правильности кода и последовательности действий
 * выполняются в RegistrationService.
 *
 * Наличие этой записи ещё не означает создание клиента банка.
 */

package com.example.minibank.registration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registration_sessions", schema = "bank")

public class RegistrationSession {

    public void initializeConfirmationCode(String codeHash) {
        if (status != RegistrationStatus.PENDING) {
            throw new IllegalStateException(
                    "Confirmation code requires PENDING status"
            );
        }

        if (confirmationCodeHash != null) {
            throw new IllegalStateException(
                    "Confirmation code is already initialized"
            );
        }

        this.confirmationCodeHash = codeHash;
        this.confirmationCodeExpiresAt = this.createdAt.plusSeconds(5 * 60);
        this.confirmationAttempts = 0;
    }

    @Id
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RegistrationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "confirmation_code_hash", length = 255)
    private String confirmationCodeHash;

    @Column(name = "confirmation_code_expires_at")
    private Instant confirmationCodeExpiresAt;

    @Column(name = "confirmation_attempts", nullable = false)
    private int confirmationAttempts;

    protected RegistrationSession() {
    }

    public RegistrationSession(
            String lastName,
            String firstName,
            String middleName,
            String phone,
            String passwordHash
    ) {
        this.sessionId = UUID.randomUUID();
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.status = RegistrationStatus.PENDING;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plusSeconds(15 * 60);
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
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

    public void recordFailedConfirmationAttempt() {
        if (confirmationAttempts >= 5) {
            throw new IllegalStateException(
                    "Confirmation attempt limit is already reached"
            );
        }

        confirmationAttempts++;
    }

    public void markPhoneConfirmed() {
        if (status != RegistrationStatus.PENDING) {
            throw new IllegalStateException(
                    "Phone confirmation requires PENDING status"
            );
        }

        this.status = RegistrationStatus.PHONE_CONFIRMED;
        this.confirmationCodeHash = null;
        this.confirmationCodeExpiresAt = null;
    }



    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getPhone() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void markCompleted() {
        if (status != RegistrationStatus.PHONE_CONFIRMED) {
            throw new IllegalStateException(
                    "Registration completion requires PHONE_CONFIRMED status"
            );
        }

        this.status = RegistrationStatus.COMPLETED;
    }


}
