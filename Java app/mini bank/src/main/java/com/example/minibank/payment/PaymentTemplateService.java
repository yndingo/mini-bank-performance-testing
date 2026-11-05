package com.example.minibank.payment;

/*
 * Создание и получение шаблонов текущего клиента.
 *
 * customerId всегда берётся из проверенной сессии.
 * Денежных операций здесь нет.
 */

import com.example.minibank.auth.SessionAuthenticationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentTemplateService {

    private final PaymentTemplateRepository repository;
    private final SessionAuthenticationService authenticationService;

    public PaymentTemplateService(
            PaymentTemplateRepository repository,
            SessionAuthenticationService authenticationService
    ) {
        this.repository = repository;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public PaymentTemplateResponse create(
            String authorizationHeader,
            PaymentTemplateRequest request
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        PaymentTemplate saved = repository.save(
                new PaymentTemplate(customerId, request)
        );

        return PaymentTemplateResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentTemplateResponse> getMyTemplates(
            String authorizationHeader
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        return repository
                .findAllByCustomerIdOrderByCreatedAtDescTemplateIdDesc(
                        customerId
                )
                .stream()
                .map(PaymentTemplateResponse::from)
                .toList();
    }
}