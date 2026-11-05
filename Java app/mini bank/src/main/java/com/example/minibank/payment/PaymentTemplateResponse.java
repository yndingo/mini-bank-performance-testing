package com.example.minibank.payment;

/*
 * DTO шаблона для ответа API.
 * JPA-сущность напрямую не возвращается.
 */

import java.time.Instant;
import java.util.UUID;

public record PaymentTemplateResponse(
        UUID templateId,
        String templateName,
        String recipientName,
        String recipientAccount,
        String recipientBankCode,
        String paymentPurpose,
        Instant createdAt
) {

    public static PaymentTemplateResponse from(
            PaymentTemplate template
    ) {
        return new PaymentTemplateResponse(
                template.getTemplateId(),
                template.getTemplateName(),
                template.getRecipientName(),
                template.getRecipientAccount(),
                template.getRecipientBankCode(),
                template.getPaymentPurpose(),
                template.getCreatedAt()
        );
    }
}