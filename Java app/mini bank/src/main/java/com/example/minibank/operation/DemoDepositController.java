package com.example.minibank.operation;

/*
 * HTTP-контроллер учебного пополнения.
 *
 * POST /api/accounts/{accountId}/demo-deposit
 *
 * Требует Bearer-токен.
 * Проверяет формат суммы через Bean Validation.
 * Возвращает HTTP 201 и результат операции.
 */

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class DemoDepositController {

    private final DemoDepositService service;

    public DemoDepositController(DemoDepositService service) {
        this.service = service;
    }

    @PostMapping("/{accountId}/demo-deposit")
    public ResponseEntity<DemoDepositResponse> deposit(
            @PathVariable("accountId") UUID accountId,
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader,
            @Valid @RequestBody DemoDepositRequest request
    ) {
        DemoDepositResponse response = service.deposit(
                accountId,
                authorizationHeader,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(response);
    }
}