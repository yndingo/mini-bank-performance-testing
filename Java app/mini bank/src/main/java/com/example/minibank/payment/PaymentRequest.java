package com.example.minibank.payment;

/*
 * Данные подготовки платежа.
 *
 * Реквизиты и назначение берутся из своего шаблона.
 * В запросе задаются шаблон, счёт списания и сумма.
 */

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(

        @NotNull
        UUID templateId,

        @NotNull
        UUID sourceAccountId,

        @NotNull
        @DecimalMin("0.01")
        @DecimalMax("1000000.00")
        @Digits(integer = 7, fraction = 2)
        BigDecimal amount
) {
}