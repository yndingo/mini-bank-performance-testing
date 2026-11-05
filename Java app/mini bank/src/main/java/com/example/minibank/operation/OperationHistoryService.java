package com.example.minibank.operation;

/*
 * Сервис истории операций своего счёта.
 *
 * Проверяет токен и владельца счёта.
 * Только после этого запрашивает историю.
 *
 * Страницы нумеруются с нуля.
 * Размер страницы фиксирован: 20 записей.
 */

import com.example.minibank.account.AccountRepository;
import com.example.minibank.auth.SessionAuthenticationService;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class OperationHistoryService {

    private static final int PAGE_SIZE = 20;

    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final SessionAuthenticationService authenticationService;

    public OperationHistoryService(
            AccountRepository accountRepository,
            OperationRepository operationRepository,
            SessionAuthenticationService authenticationService
    ) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional(readOnly = true)
    public List<OperationHistoryResponse> getHistory(
            UUID accountId,
            String authorizationHeader,
            int page
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        if (page < 0 || page > 10000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be between 0 and 10000"
            );
        }

        accountRepository.findOwnedAccountId(accountId, customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found"
                ));

        return operationRepository.findAccountHistory(
                        accountId,
                        PageRequest.of(page, PAGE_SIZE)
                )
                .stream()
                .map(operation -> OperationHistoryResponse.from(
                        operation,
                        accountId
                ))
                .toList();
    }
}