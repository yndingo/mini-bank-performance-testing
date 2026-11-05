package com.example.minibank.payment;

/*
 * Двухэтапный платёж по своему шаблону.
 *
 * prepare():
 * проверяет владельца шаблона и счёта,
 * копирует реквизиты и создаёт PENDING на 5 минут.
 * Баланс не изменяет.
 *
 * confirm():
 * блокирует платёж и счёт владельца,
 * проверяет срок и остаток, выполняет списание,
 * сохраняет TEMPLATE_PAYMENT и результат.
 *
 * Повтор подтверждения не создаёт новое списание.
 */

import com.example.minibank.account.Account;
import com.example.minibank.account.AccountRepository;
import com.example.minibank.auth.SessionAuthenticationService;
import com.example.minibank.operation.Operation;
import com.example.minibank.operation.OperationRepository;
import com.example.minibank.operation.TransferSessionStatus;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentTemplateRepository templateRepository;
    private final PaymentSessionRepository sessionRepository;
    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final SessionAuthenticationService authenticationService;

    public PaymentService(
            PaymentTemplateRepository templateRepository,
            PaymentSessionRepository sessionRepository,
            AccountRepository accountRepository,
            OperationRepository operationRepository,
            SessionAuthenticationService authenticationService
    ) {
        this.templateRepository = templateRepository;
        this.sessionRepository = sessionRepository;
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public PaymentResponse prepare(
            String authorizationHeader,
            PaymentRequest request
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        PaymentTemplate template = templateRepository
                .findByTemplateIdAndCustomerId(
                        request.templateId(),
                        customerId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment template not found"
                ));

        accountRepository.findOwnedAccountId(
                request.sourceAccountId(),
                customerId
        ).orElseThrow(PaymentService::accountNotFound);

        PaymentSession session = sessionRepository.save(
                new PaymentSession(
                        template,
                        request.sourceAccountId(),
                        request.amount()
                )
        );

        return PaymentResponse.from(session);
    }

    @Transactional
    public PaymentResponse confirm(
            String authorizationHeader,
            UUID transactionId
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        PaymentSession session = sessionRepository
                .findOwnedForUpdate(transactionId, customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found"
                ));

        if (session.getStatus() == TransferSessionStatus.COMPLETED) {
            return PaymentResponse.from(session);
        }

        if (!Instant.now().isBefore(session.getExpiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Payment confirmation expired"
            );
        }

        Account source = accountRepository.findOwnedAccountForUpdate(
                session.getSourceAccountId(),
                customerId
        ).orElseThrow(PaymentService::accountNotFound);

        if (!"RUB".equals(source.getCurrency())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unsupported account currency"
            );
        }

        if (source.getBalance().compareTo(session.getAmount()) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient funds"
            );
        }

        source.debit(session.getAmount());

        Operation operation = operationRepository.save(
                Operation.templatePayment(
                        source.getAccountId(),
                        session.getAmount()
                )
        );

        session.complete(
                operation.getOperationId(),
                source.getBalance(),
                operation.getCreatedAt()
        );

        return PaymentResponse.from(session);
    }

    private static ResponseStatusException accountNotFound() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Account not found"
        );
    }
}