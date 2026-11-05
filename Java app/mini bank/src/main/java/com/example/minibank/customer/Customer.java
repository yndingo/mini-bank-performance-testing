/*
 * JPA-сущность клиента мини-банка.
 * Соответствует таблице bank.customers в PostgreSQL.
 *
 * Хранит:
 * - постоянный идентификатор клиента customerId;
 * - идентификатор регистрационной сессии, из которой создан клиент;
 * - ФИО и уникальный номер телефона;
 * - хеш пароля;
 * - время создания клиента.
 *
 * customerId отличается от sessionId:
 * sessionId связывает этапы регистрации,
 * customerId идентифицирует уже созданного клиента.
 *
 * Конструктор генерирует UUID клиента и задаёт время создания.
 * Пароль поступает в конструктор уже в виде хеша:
 * повторно хешировать его здесь не нужно.
 *
 * Связь с регистрационной сессией хранится как UUID.
 * В БД внешний ключ требует существования этой сессии,
 * а UNIQUE не позволяет создать из неё двух клиентов.
 *
 * Сущность сама не завершает регистрацию и не создаёт счета.
 * Эти действия будет выполнять сервис в одной транзакции.
 *
 * ВАЖНО: содержит хеш пароля.
 * Не возвращать сущность целиком из контроллера
 * и не выводить её целиком в журналы.
 */

package com.example.minibank.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers", schema = "bank")
public class Customer {

    @Id
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(
            name = "registration_session_id",
            nullable = false,
            unique = true
    )
    private UUID registrationSessionId;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Customer() {
    }

    public Customer(
            UUID registrationSessionId,
            String lastName,
            String firstName,
            String middleName,
            String phone,
            String passwordHash
    ) {
        this.customerId = UUID.randomUUID();
        this.registrationSessionId = registrationSessionId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getRegistrationSessionId() {
        return registrationSessionId;
    }

    public String getPhone() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}