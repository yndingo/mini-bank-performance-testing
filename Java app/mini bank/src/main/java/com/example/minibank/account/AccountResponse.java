package com.example.minibank.account;

/*
 * DTO счёта для ответа API.
 *
 * Возвращает идентификатор, учебный номер,
 * валюту и баланс.
 *
 * JPA-сущность напрямую наружу не передаётся.
 * customerId не требуется: endpoint возвращает
 * только счета текущего клиента.
 */

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID accountId,
        String accountNumber,
        String currency,
        BigDecimal balance
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getCurrency(),
                account.getBalance()
        );
    }
}