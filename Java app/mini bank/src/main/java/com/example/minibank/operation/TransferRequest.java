package com.example.minibank.operation;

/*
 * DTO перевода.
 *
 * Счёт списания задаётся UUID.
 * Получатель задаётся учебным номером счёта.
 * Владельца счёта списания сервис проверяет по токену.
 *
 * Учебный лимит: 1 000 000.00 RUB за перевод.
 */

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(

        @NotNull
        UUID sourceAccountId,

        @NotNull
        @Pattern(regexp = "^ACC-[0-9a-f]{32}$")
        String destinationAccountNumber,

        @NotNull
        @DecimalMin("0.01")
        @DecimalMax("1000000.00")
        @Digits(integer = 7, fraction = 2)
        BigDecimal amount
) {
}