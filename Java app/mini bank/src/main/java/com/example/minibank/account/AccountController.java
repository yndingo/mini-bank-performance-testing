package com.example.minibank.account;

/*
 * HTTP-контроллер счетов.
 *
 * GET /api/accounts:
 * возвращает счета владельца проверенного токена.
 *
 * Токен передаётся в Authorization: Bearer ...
 * При отсутствии заголовка запрос всё равно попадает
 * в сервис, который возвращает единообразный HTTP 401.
 */

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader
    ) {
        List<AccountResponse> accounts =
                service.getMyAccounts(authorizationHeader);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(accounts);
    }
}