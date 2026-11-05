package com.example.minibank.auth;

/*
 * Сервис авторизации.
 *
 * startLogin():
 * - проверяет телефон и пароль клиента;
 * - генерирует шестизначный код;
 * - создаёт сессию PENDING со сроком 5 минут;
 * - сохраняет только хеш кода;
 * - возвращает идентификатор сессии и срок.
 *
 * При неверных учётных данных возвращает одинаковую ошибку,
 * независимо от существования телефона.
 *
 * При включённой настройке demo-sms-enabled
 * показывает код в журнале локального стенда.
 * Это имитация SMS, не реальная отправка.
 *
 * Подтверждение кода и выдача токена будут добавлены отдельно.
 */

import com.example.minibank.customer.Customer;
import com.example.minibank.customer.CustomerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import java.time.Instant;
import java.util.Base64;

@Service
public class AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    private final CustomerRepository customerRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private final boolean demoSmsEnabled;
    private final String dummyPasswordHash;

    public AuthService(
            CustomerRepository customerRepository,
            AuthSessionRepository authSessionRepository,
            @Value("${app.auth.demo-sms-enabled:false}")
            boolean demoSmsEnabled
    ) {
        this.customerRepository = customerRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(10);
        this.secureRandom = new SecureRandom();
        this.demoSmsEnabled = demoSmsEnabled;
        this.dummyPasswordHash = passwordEncoder.encode(
                UUID.randomUUID().toString()
        );
    }

    @Transactional
    public LoginResponse startLogin(LoginRequest request) {
        Optional<Customer> customerOptional =
                customerRepository.findByPhone(request.phone());

        String storedHash = customerOptional
                .map(Customer::getPasswordHash)
                .orElse(dummyPasswordHash);

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                storedHash
        );

        if (customerOptional.isEmpty() || !passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid phone or password"
            );
        }

        Customer customer = customerOptional.get();

        String confirmationCode = String.format(
                Locale.ROOT,
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        AuthSession session = new AuthSession(
                customer.getCustomerId(),
                passwordEncoder.encode(confirmationCode)
        );

        AuthSession saved = authSessionRepository.save(session);

        if (demoSmsEnabled) {
            log.warn(
                    "LOCAL DEMO LOGIN SMS: sessionId={}, code={}",
                    saved.getSessionId(),
                    confirmationCode
            );
        }

        return new LoginResponse(
                saved.getSessionId(),
                saved.getStatus(),
                saved.getExpiresAt()
        );
    }

    @Transactional(noRollbackFor = IncorrectLoginCodeException.class)
    public ConfirmLoginResponse confirmLogin(
            UUID sessionId,
            ConfirmLoginRequest request
    ) {
        AuthSession session = authSessionRepository
                .findBySessionIdForUpdate(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Authentication session not found"
                ));

        if (session.getStatus() != AuthSessionStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Authentication session is not awaiting confirmation"
            );
        }

        Instant now = Instant.now();

        if (!now.isBefore(session.getExpiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Authentication session expired"
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
            throw new IncorrectLoginCodeException();
        }

        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        String accessToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        session.activate(AccessTokenHasher.hash(accessToken));

        return new ConfirmLoginResponse(
                session.getSessionId(),
                session.getStatus(),
                accessToken,
                "Bearer",
                session.getExpiresAt()
        );
    }

}