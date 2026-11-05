package com.example.minibank.payment;

/*
 * JPA-сущность шаблона платежа.
 * Соответствует bank.payment_templates.
 *
 * Хранит владельца, название, реквизиты
 * и назначение платежа.
 *
 * Сам шаблон денег не списывает.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "payment_templates", schema = "bank")
public class PaymentTemplate {

    @Id
    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "recipient_name", nullable = false, length = 200)
    private String recipientName;

    @Column(name = "recipient_account", nullable = false, length = 20)
    private String recipientAccount;

    @Column(name = "recipient_bank_code", nullable = false, length = 9)
    private String recipientBankCode;

    @Column(name = "payment_purpose", nullable = false, length = 250)
    private String paymentPurpose;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PaymentTemplate() {
    }

    public PaymentTemplate(
            UUID customerId,
            PaymentTemplateRequest request
    ) {
        this.templateId = UUID.randomUUID();
        this.customerId = customerId;
        this.templateName = request.templateName().strip();
        this.recipientName = request.recipientName().strip();
        this.recipientAccount = request.recipientAccount();
        this.recipientBankCode = request.recipientBankCode();
        this.paymentPurpose = request.paymentPurpose().strip();
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public String getRecipientBankCode() {
        return recipientBankCode;
    }

    public String getPaymentPurpose() {
        return paymentPurpose;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}