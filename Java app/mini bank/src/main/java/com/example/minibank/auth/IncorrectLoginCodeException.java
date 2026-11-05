package com.example.minibank.auth;

/*
 * Неверный код подтверждения входа.
 *
 * Возвращает HTTP 400.
 * В сервисе исключён откат транзакции для этой ошибки,
 * чтобы увеличение счётчика попыток сохранилось в БД.
 */

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "Incorrect login confirmation code"
)
public class IncorrectLoginCodeException extends RuntimeException {

    public IncorrectLoginCodeException() {
        super("Incorrect login confirmation code");
    }
}