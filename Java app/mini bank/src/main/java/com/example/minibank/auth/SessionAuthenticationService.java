package com.example.minibank.auth;

/*
 * Проверка и отзыв сессии по токену доступа.
 *
 * requireCustomerId():
 * проверяет токен и возвращает владельца активной сессии.
 *
 * logout():
 * проверяет токен и отзывает текущую сессию.
 * Другие сессии клиента не затрагиваются.
 *
 * requireActiveSession():
 * общая внутренняя проверка заголовка, токена,
 * статуса ACTIVE и срока действия.
 *
 * Исходный токен не сохраняется и не журналируется.
 *
 *
 * Срок доступа фиксирован при активации сессии.
 * Успешные запросы не продлевают expiresAt.
 * При истечении срока или недействительной сессии
 * возвращается HTTP 401 с заголовком WWW-Authenticate: Bearer.
 */

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class SessionAuthenticationService {

    private static final Pattern ACCESS_TOKEN_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]{43}$");

    private final AuthSessionRepository authSessionRepository;

    public SessionAuthenticationService(
            AuthSessionRepository authSessionRepository
    ) {
        this.authSessionRepository = authSessionRepository;
    }

    @Transactional(readOnly = true)
    public UUID requireCustomerId(String authorizationHeader) {
        AuthSession session = requireActiveSession(
                authorizationHeader
        );

        return session.getCustomerId();
    }

    @Transactional
    public void logout(String authorizationHeader) {
        AuthSession session = requireActiveSession(
                authorizationHeader
        );

        session.revoke();
    }

    private AuthSession requireActiveSession(
            String authorizationHeader
    ) {
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(
                true, 0, "Bearer ", 0, 7
        )) {
            throw unauthorized();
        }

        String accessToken = authorizationHeader.substring(7);

        if (!ACCESS_TOKEN_PATTERN.matcher(accessToken).matches()) {
            throw unauthorized();
        }

        String tokenHash = AccessTokenHasher.hash(accessToken);

        AuthSession session = authSessionRepository
                .findByAccessTokenHash(tokenHash)
                .orElseThrow(
                        SessionAuthenticationService::unauthorized
                );

        if (session.getStatus() != AuthSessionStatus.ACTIVE) {
            throw unauthorized();
        }

        if (!Instant.now().isBefore(session.getExpiresAt())) {
            throw unauthorized();
        }

        return session;
    }

    private static ResponseStatusException unauthorized() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authentication required"
        ) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();

                headers.set(
                        HttpHeaders.WWW_AUTHENTICATE,
                        "Bearer"
                );

                return headers;
            }
        };
    }
}