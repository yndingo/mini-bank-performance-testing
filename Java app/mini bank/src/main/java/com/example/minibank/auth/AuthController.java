package com.example.minibank.auth;

/*
 * HTTP-контроллер авторизации.
 *
 * POST /api/auth/login:
 * проверяет телефон и пароль, создаёт сессию PENDING.
 *
 * POST /api/auth/login/{sessionId}/confirm:
 * подтверждает код, активирует сессию и возвращает токен.
 *
 * POST /api/auth/logout:
 * отзывает текущую активную сессию по Bearer-токену.
 * При успехе возвращает HTTP 204 без тела.
 *
 * Пароли, коды и токены не журналируются контроллером.
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;
    private final SessionAuthenticationService authenticationService;

    public AuthController(
            AuthService service,
            SessionAuthenticationService authenticationService
    ) {
        this.service = service;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return service.startLogin(request);
    }

    @PostMapping("/login/{sessionId}/confirm")
    public ConfirmLoginResponse confirmLogin(
            @PathVariable("sessionId") UUID sessionId,
            @Valid @RequestBody ConfirmLoginRequest request
    ) {
        return service.confirmLogin(sessionId, request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader
    ) {
        authenticationService.logout(authorizationHeader);

        return ResponseEntity.noContent().build();
    }
}