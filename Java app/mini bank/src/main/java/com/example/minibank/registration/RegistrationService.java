/*
 * Сервис бизнес-логики регистрации.
 * Связывает входные данные, сущность и репозиторий.
 *
 * start():
 * - хеширует пароль;
 * - создаёт регистрационную сессию;
 * - генерирует случайный шестизначный код;
 * - сохраняет хеш кода и сессию в БД;
 * - возвращает sessionId, статус и срок действия сессии.
 *
 * При app.registration.demo-sms-enabled=true выводит код
 * в журнал как локальную имитацию SMS.
 * Это демонстрационный режим, не реальная отправка SMS.
 *
 * confirmPhone():
 * - загружает сессию с блокировкой;
 * - проверяет статус, сроки и лимит попыток;
 * - сравнивает код с сохранённым хешем;
 * - при ошибке увеличивает счётчик;
 * - при успехе устанавливает PHONE_CONFIRMED
 *   и очищает данные использованного кода.
 *
 * Методы работают внутри транзакций.
 * Для IncorrectConfirmationCodeException отключён откат,
 * чтобы неверная попытка сохранилась в БД.
 *
 * HTTP-маршруты объявлены не здесь, а в контроллере.
 * Создание клиента, счёта и карты пока не реализовано.
 */

package com.example.minibank.registration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class RegistrationService {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationService.class);

    private final RegistrationSessionRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private final boolean demoSmsEnabled;

    public RegistrationService(
            RegistrationSessionRepository repository,
            @Value("${app.registration.demo-sms-enabled:false}")
            boolean demoSmsEnabled
    ) {
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder(10);
        this.secureRandom = new SecureRandom();
        this.demoSmsEnabled = demoSmsEnabled;
    }

    @Transactional
    public StartRegistrationResponse start(
            StartRegistrationRequest request
    ) {
        String passwordHash = passwordEncoder.encode(request.password());

        RegistrationSession session = new RegistrationSession(
                request.lastName().strip(),
                request.firstName().strip(),
                request.middleName() == null
                        ? null
                        : request.middleName().strip(),
                request.phone(),
                passwordHash
        );

        String confirmationCode = String.format(
                Locale.ROOT,
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        session.initializeConfirmationCode(
                passwordEncoder.encode(confirmationCode)
        );

        RegistrationSession saved = repository.save(session);

        if (demoSmsEnabled) {
            log.warn(
                    "LOCAL DEMO SMS: sessionId={}, code={}",
                    saved.getSessionId(),
                    confirmationCode
            );
        }

        return new StartRegistrationResponse(
                saved.getSessionId(),
                saved.getStatus(),
                saved.getExpiresAt()
        );
    }

    @Transactional(
            noRollbackFor = IncorrectConfirmationCodeException.class
    )
    public ConfirmPhoneResponse confirmPhone(
            UUID sessionId,
            ConfirmPhoneRequest request
    ) {
        RegistrationSession session = repository
                .findBySessionIdForUpdate(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registration session not found"
                ));

        if (session.getStatus() != RegistrationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Registration is not awaiting phone confirmation"
            );
        }

        Instant now = Instant.now();

        if (!now.isBefore(session.getExpiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Registration session expired"
            );
        }

        if (session.getConfirmationCodeHash() == null
                || session.getConfirmationCodeExpiresAt() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Confirmation code is not initialized"
            );
        }

        if (!now.isBefore(session.getConfirmationCodeExpiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Confirmation code expired"
            );
        }

        if (session.getConfirmationAttempts() >= 5) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Confirmation attempt limit reached"
            );
        }

        boolean codeMatches = passwordEncoder.matches(
                request.code(),
                session.getConfirmationCodeHash()
        );

        if (!codeMatches) {
            session.recordFailedConfirmationAttempt();
            throw new IncorrectConfirmationCodeException();
        }

        session.markPhoneConfirmed();

        return new ConfirmPhoneResponse(
                session.getSessionId(),
                session.getStatus()
        );
    }
}