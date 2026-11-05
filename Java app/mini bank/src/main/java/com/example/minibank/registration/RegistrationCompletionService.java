package com.example.minibank.registration;

/*
 * Сервис третьего этапа регистрации.
 *
 * Проверяет подтверждённую сессию и создаёт:
 * - клиента;
 * - его первый счёт с нулевым балансом;
 * - учебную карту к этому счёту.
 *
 * Затем переводит регистрационную сессию в COMPLETED.
 *
 * Все действия выполняются одной транзакцией.
 * При ошибке изменения откатываются целиком.
 *
 * Сессия читается с блокировкой, чтобы параллельные
 * запросы не завершили одну регистрацию дважды.
 */

import com.example.minibank.account.Account;
import com.example.minibank.account.AccountRepository;
import com.example.minibank.card.Card;
import com.example.minibank.card.CardRepository;
import com.example.minibank.customer.Customer;
import com.example.minibank.customer.CustomerRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class RegistrationCompletionService {

    private final RegistrationSessionRepository sessionRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    public RegistrationCompletionService(
            RegistrationSessionRepository sessionRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            CardRepository cardRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
    }

    @Transactional
    public CompleteRegistrationResponse complete(UUID sessionId) {
        RegistrationSession session = sessionRepository
                .findBySessionIdForUpdate(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registration session not found"
                ));

        if (session.getStatus() != RegistrationStatus.PHONE_CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Registration is not ready for completion"
            );
        }

        if (!Instant.now().isBefore(session.getExpiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Registration session expired"
            );
        }

        if (customerRepository.existsByPhone(session.getPhone())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone is already registered"
            );
        }

        Customer customer = new Customer(
                session.getSessionId(),
                session.getLastName(),
                session.getFirstName(),
                session.getMiddleName(),
                session.getPhone(),
                session.getPasswordHash()
        );

        Customer savedCustomer;

        try {
            savedCustomer = customerRepository.saveAndFlush(customer);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer data conflicts with existing records",
                    exception
            );
        }

        Account account = accountRepository.saveAndFlush(
                new Account(savedCustomer.getCustomerId())
        );

        Card card = cardRepository.saveAndFlush(
                new Card(account.getAccountId())
        );

        session.markCompleted();

        return new CompleteRegistrationResponse(
                session.getSessionId(),
                session.getStatus(),
                savedCustomer.getCustomerId(),
                account.getAccountId(),
                account.getAccountNumber(),
                account.getCurrency(),
                account.getBalance(),
                card.getCardId(),
                card.getCardNumber(),
                card.getExpiresOn()
        );
    }
}