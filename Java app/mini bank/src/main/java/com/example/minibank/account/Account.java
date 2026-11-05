package com.example.minibank.account;

/*
 * JPA-сущность банковского счёта.
 * Соответствует таблице bank.accounts.
 *
 * Хранит владельца, учебный номер счёта, валюту,
 * баланс и время создания.
 *
 * При создании задаёт UUID, демонстрационный номер,
 * валюту RUB и нулевой баланс.
 *
 * customerId связывает счёт с клиентом.
 * Реальную принадлежность клиента проверяет внешний ключ в БД.
 *
 * Методов изменения баланса пока нет:
 * добавим их вместе с логикой денежных операций.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import java.math.RoundingMode;

@Entity
@Table(name = "accounts", schema = "bank")
public class Account {

    @Id
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(
            name = "account_number",
            nullable = false,
            unique = true,
            length = 40
    )
    private String accountNumber;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Account() {
    }

    public Account(UUID customerId) {
        this.accountId = UUID.randomUUID();
        this.customerId = customerId;
        this.accountNumber =
                "ACC-" + accountId.toString().replace("-", "");
        this.currency = "RUB";
        this.balance = new BigDecimal("0.00");
        this.createdAt = Instant.now();
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void credit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Credit amount must be positive"
            );
        }

        BigDecimal normalizedAmount = amount.setScale(
                2,
                RoundingMode.UNNECESSARY
        );

        BigDecimal newBalance = balance.add(normalizedAmount);

        if (newBalance.precision() > 19) {
            throw new IllegalArgumentException(
                    "Resulting balance exceeds database precision"
            );
        }

        this.balance = newBalance;
    }

    public void debit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Debit amount must be positive"
            );
        }

        BigDecimal normalizedAmount = amount.setScale(
                2,
                RoundingMode.UNNECESSARY
        );

        if (balance.compareTo(normalizedAmount) < 0) {
            throw new IllegalStateException(
                    "Insufficient funds"
            );
        }

        this.balance = balance.subtract(normalizedAmount);
    }


}