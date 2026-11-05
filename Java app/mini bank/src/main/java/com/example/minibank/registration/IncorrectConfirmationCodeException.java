//отдельная ошибка неверного кода
//нужно сохранить увеличение счётчика попыток, хотя клиент получит HTTP 400.
/*
 * Исключение для неверного кода подтверждения.
 *
 * Преобразуется в HTTP 400 Bad Request.
 * Используется, когда код имеет допустимый формат,
 * но не совпадает с кодом данной регистрационной сессии.
 *
 * Перед выбрасыванием исключения сервис увеличивает
 * счётчик неверных попыток.
 *
 * В confirmPhone() это исключение указано в noRollbackFor:
 * HTTP-запрос завершается ошибкой, но увеличение счётчика
 * сохраняется в БД.
 *
 * Не используется для истёкшего кода, отсутствующей сессии,
 * неверного статуса или превышения лимита попыток.
 */

package com.example.minibank.registration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "Incorrect confirmation code"
)
public class IncorrectConfirmationCodeException
        extends RuntimeException {

    public IncorrectConfirmationCodeException() {
        super("Incorrect confirmation code");
    }
}