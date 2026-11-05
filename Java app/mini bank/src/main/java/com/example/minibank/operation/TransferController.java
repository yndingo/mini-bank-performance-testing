package com.example.minibank.operation;

/*
 * Двухэтапный внутренний перевод.
 *
 * POST /api/transfers/prepare
 * создаёт ожидающий перевод.
 *
 * POST /api/transfers/{transactionId}/confirm
 * выполняет подготовленный перевод.
 *
 * Оба этапа требуют токен владельца.
 * Отдельный Idempotency-Key больше не требуется:
 * повтор подтверждения связан через transactionId.
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
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferSessionService service;

    public TransferController(TransferSessionService service) {
        this.service = service;
    }

    @PostMapping("/prepare")
    public ResponseEntity<PrepareTransferResponse> prepare(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader,
            @Valid @RequestBody TransferRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(service.prepare(authorizationHeader, request));
    }

    @PostMapping("/{transactionId}/confirm")
    public ResponseEntity<ConfirmTransferResponse> confirm(
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