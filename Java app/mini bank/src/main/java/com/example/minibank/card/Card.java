package com.example.minibank.card;

/*
 * JPA-сущность учебной банковской карты.
 * Соответствует таблице bank.cards.
 *
 * Хранит идентификатор карты, связанный счёт,
 * демонстрационный номер, срок действия и время создания.
 *
 * Баланса у карты нет: деньги находятся на связанном счёте.
 * PIN и CVV в этой учебной модели не используются.
 *
 * Номер с префиксом DEMO- не является реальным
 * номером платёжной карты.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "cards", schema = "bank")
public class Card {

    @Id
    @Column(name = "card_id", nullable = false)
    private UUID cardId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(
            name = "card_number",
            nullable = false,
            unique = true,
            length = 40
    )
    private String cardNumber;

    @Column(name = "expires_on", nullable = false)
    private LocalDate expiresOn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Card() {
    }

    public Card(UUID accountId) {
        this.cardId = UUID.randomUUID();
        this.accountId = accountId;
        this.cardNumber =
                "DEMO-" + cardId.toString().replace("-", "");
        this.expiresOn = LocalDate.now(ZoneOffset.UTC).plusYears(3);
        this.createdAt = Instant.now();
    }

    public UUID getCardId() {
        return cardId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public LocalDate getExpiresOn() {
        return expiresOn;
    }
}