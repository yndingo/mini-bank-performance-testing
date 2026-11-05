package com.example.minibank.payment;

/*
 * POST /api/payment-templates — создать свой шаблон.
 * GET /api/payment-templates — получить свои шаблоны.
 *
 * Оба маршрута требуют Bearer-токен.
 */

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payment-templates")
public class PaymentTemplateController {

    private final PaymentTemplateService service;

    public PaymentTemplateController(
            PaymentTemplateService service
    ) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PaymentTemplateResponse> create(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader,
            @Valid @RequestBody PaymentTemplateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(service.create(authorizationHeader, request));
    }

    @GetMapping
    public ResponseEntity<List<PaymentTemplateResponse>> getMyTemplates(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader
    ) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(service.getMyTemplates(authorizationHeader));
    }
}