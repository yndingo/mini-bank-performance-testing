package com.example.minibank.registration;

/*
 * REST-контроллер многошаговой регистрации.
 * Общий префикс маршрутов: /api/registrations.
 *
 * POST /api/registrations:
 * начало регистрации, ответ 201 Created.
 *
 * POST /api/registrations/{sessionId}/confirm-phone:
 * подтверждение телефона, ответ 200 OK.
 *
 * POST /api/registrations/{sessionId}/complete:
 * создание клиента, счёта и карты, ответ 201 Created.
 *
 * Контроллер принимает HTTP-запросы, проверяет формат
 * входных данных и передаёт выполнение сервисам.
 * Бизнес-логика и работа с БД находятся не здесь.
 */

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService service;
    private final RegistrationCompletionService completionService;

    public RegistrationController(
            RegistrationService service,
            RegistrationCompletionService completionService
    ) {
        this.service = service;
        this.completionService = completionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StartRegistrationResponse start(
            @Valid @RequestBody StartRegistrationRequest request
    ) {
        return service.start(request);
    }

    @PostMapping("/{sessionId}/confirm-phone")
    public ConfirmPhoneResponse confirmPhone(
            @PathVariable("sessionId") UUID sessionId,
            @Valid @RequestBody ConfirmPhoneRequest request
    ) {
        return service.confirmPhone(sessionId, request);
    }

    @PostMapping("/{sessionId}/complete")
    @ResponseStatus(HttpStatus.CREATED)
    public CompleteRegistrationResponse complete(
            @PathVariable("sessionId") UUID sessionId
    ) {
        return completionService.complete(sessionId);
    }
}