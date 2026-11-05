package com.example.minibank.account;

/*
 * Сервис получения счетов текущего клиента.
 *
 * Сначала проверяет токен через сервис аутентификации.
 * Затем запрашивает только счета владельца сессии.
 *
 * Не принимает customerId из HTTP-запроса.
 * Не выполняет денежных операций.
 */

import com.example.minibank.auth.SessionAuthenticationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final SessionAuthenticationService authenticationService;

    public AccountService(
            AccountRepository accountRepository,
            SessionAuthenticationService authenticationService
    ) {
        this.accountRepository = accountRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(
            String authorizationHeader
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        return accountRepository.findAllByCustomerId(customerId)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }
}