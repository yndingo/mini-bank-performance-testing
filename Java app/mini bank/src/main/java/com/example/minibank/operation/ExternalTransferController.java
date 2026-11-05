package com.example.minibank.operation;

/*
 * Двухэтапный внешний перевод.
 *
 * POST /api/external-transfers/prepare
 * POST /api/external-transfers/{transactionId}/confirm
 *
 * Оба этапа требуют токен владельца.
 * Подтверждение не принимает реквизиты повторно.
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
@RequestMapping("/api/external-transfers")
public class ExternalTransferController {

    private final ExternalTransferService service;

    public ExternalTransferController(
            ExternalTransferService service
    ) {
        this.service = service;
    }

    @PostMapping("/prepare")
    public ResponseEntity<ExternalTransferResponse> prepare(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader,
            @Valid @RequestBody ExternalTransferRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(service.prepare(authorizationHeader, request));
    }

    @PostMapping("/{transactionId}/confirm")
    public ResponseEntity<ExternalTransferResponse> confirm(
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