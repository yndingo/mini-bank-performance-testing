package com.example.minibank.operation;

/*
 * Двухэтапный внешний перевод с имитацией отправки.
 *
 * prepare():
 * проверяет принадлежность счёта и сохраняет PENDING.
 * Деньги не списывает и не резервирует.
 *
 * confirm():
 * блокирует сессию и счёт владельца,
 * проверяет срок, валюту и остаток,
 * списывает деньги и сохраняет EXTERNAL_TRANSFER.
 *
 * Повтор подтверждения возвращает сохранённый результат.
 * Все изменения подтверждения — в одной транзакции.
 */

import com.example.minibank.account.Account;
import com.example.minibank.account.AccountRepository;
import com.example.minibank.auth.SessionAuthenticationService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class ExternalTransferService {

    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final ExternalTransferSessionRepository sessionRepository;
    private final SessionAuthenticationService authenticationService;

    public ExternalTransferService(
            AccountRepository accountRepository,
            OperationRepository operationRepository,
            ExternalTransferSessionRepository sessionRepository,
            SessionAuthenticationService authenticationService
    ) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        this.sessionRepository = sessionRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public ExternalTransferResponse prepare(
            String authorizationHeader,
            ExternalTransferRequest request
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        accountRepository.findOwnedAccountId(
                request.sourceAccountId(),
                customerId
        ).orElseThrow(ExternalTransferService::accountNotFound);

        ExternalTransferSession session = sessionRepository.save(
                new ExternalTransferSession(
                        customerId,
                        request.sourceAccountId(),
                        request.recipientName(),
                        request.recipientAccount(),
                        request.recipientBankCode(),
                        request.amount()
                )
        );

        return ExternalTransferResponse.from(session);
    }

    @Transactional
    public ExternalTransferResponse confirm(
            String authorizationHeader,
            UUID transactionId
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        ExternalTransferSession session = sessionRepository
                .findOwnedForUpdate(transactionId, customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "External transfer not found"
                ));

        if (session.getStatus() == TransferSessionStatus.COMPLETED) {
            return ExternalTransferResponse.from(session);
        }

        if (!Instant.now().isBefore(session.getExpiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "External transfer confirmation expired"
            );
        }

        Account source = accountRepository.findOwnedAccountForUpdate(
                session.getSourceAccountId(),
                customerId
        ).orElseThrow(ExternalTransferService::accountNotFound);

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
                Operation.externalTransfer(
                        source.getAccountId(),
                        session.getAmount()
                )
        );

        session.complete(
                operation.getOperationId(),
                source.getBalance(),
                operation.getCreatedAt()
        );

        return ExternalTransferResponse.from(session);
    }

    private static ResponseStatusException accountNotFound() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Account not found"
        );
    }
}