package com.example.minibank.payment;

/*
 * DTO создания шаблона платежа.
 *
 * Владелец определяется по токену, а не из тела запроса.
 * Сумма и счёт списания задаются при подготовке платежа.
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PaymentTemplateRequest(

        @NotBlank
        @Size(max = 100)
        String templateName,

        @NotBlank
        @Size(max = 200)
        String recipientName,

        @NotNull
        @Pattern(regexp = "^[0-9]{20}$")
        String recipientAccount,

        @NotNull
        @Pattern(regexp = "^[0-9]{9}$")
        String recipientBankCode,

        @NotBlank
        @Size(max = 250)
        String paymentPurpose
) {
}