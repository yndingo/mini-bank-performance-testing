package com.example.minibank.payment;

/*
 * Репозиторий шаблонов.
 *
 * Список ограничивается владельцем.
 * Поиск по templateId и customerId понадобится
 * при подготовке платежа по своему шаблону.
 */

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTemplateRepository
        extends JpaRepository<PaymentTemplate, UUID> {

    List<PaymentTemplate>
    findAllByCustomerIdOrderByCreatedAtDescTemplateIdDesc(
            UUID customerId
    );

    Optional<PaymentTemplate> findByTemplateIdAndCustomerId(
            UUID templateId,
            UUID customerId
    );
}