package com.example.minibank.operation;

/*
 * Данные подготовки внешнего перевода.
 *
 * Реквизиты проверяются только по формату.
 * Существование внешнего счёта и банка не проверяется.
 */

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ExternalTransferRequest(

        @NotNull
        UUID sourceAccountId,

        @NotBlank
        @Size(max = 200)
        String recipientName,

        @NotNull
        @Pattern(regexp = "^[0-9]{20}$")
        String recipientAccount,

        @NotNull
        @Pattern(regexp = "^[0-9]{9}$")
        String recipientBankCode,

        @NotNull
        @DecimalMin("0.01")
        @DecimalMax("1000000.00")
        @Digits(integer = 7, fraction = 2)
        BigDecimal amount
) {
}