package com.example.minibank.operation;

/*
 * HTTP-контроллер истории операций.
 *
 * GET /api/accounts/{accountId}/operations?page=0
 *
 * Требует Bearer-токен владельца счёта.
 * Возвращает до 20 операций, новые первыми.
 */

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class OperationHistoryController {

    private final OperationHistoryService service;

    public OperationHistoryController(
            OperationHistoryService service
    ) {
        this.service = service;
    }

    @GetMapping("/{accountId}/operations")
    public ResponseEntity<List<OperationHistoryResponse>> getHistory(
            @PathVariable("accountId") UUID accountId,
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader,
            @RequestParam(value = "page", defaultValue = "0")
            int page
    ) {
        List<OperationHistoryResponse> response =
                service.getHistory(
                        accountId,
                        authorizationHeader,
                        page
                );

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(response);
    }
}