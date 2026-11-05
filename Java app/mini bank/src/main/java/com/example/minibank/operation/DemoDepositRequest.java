package com.example.minibank.operation;

/*
 * DTO учебного пополнения.
 *
 * Сумма: от 0.01 до 1 000 000.00 RUB за запрос.
 * Не более двух знаков после десятичной точки.
 *
 * Это ограничение учебного API, не банковский норматив.
 */

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DemoDepositRequest(

        @NotNull
        @DecimalMin("0.01")
        @DecimalMax("1000000.00")
        @Digits(integer = 7, fraction = 2)
        BigDecimal amount
) {
}