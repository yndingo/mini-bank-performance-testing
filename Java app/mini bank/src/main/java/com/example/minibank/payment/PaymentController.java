package com.example.minibank.payment;

/*
 * POST /api/payments/prepare
 * POST /api/payments/{transactionId}/confirm
 *
 * Оба этапа требуют Bearer-токен владельца.
 * Подтверждение использует сохранённые параметры.
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
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/prepare")
    public ResponseEntity<PaymentResponse> prepare(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader,
            @Valid @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(service.prepare(authorizationHeader, request));
    }

    @PostMapping("/{transactionId}/confirm")
    public ResponseEntity<PaymentResponse> confirm(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader,
            @PathVariable("transactionId") UUID transactionId
    ) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(service.confirm(
                        authorizationHeader,
                        transactionId
                ));
    }
}