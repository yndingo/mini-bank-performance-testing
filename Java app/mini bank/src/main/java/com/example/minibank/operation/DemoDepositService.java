package com.example.minibank.operation;

/*
 * Сервис учебного пополнения.
 *
 * Работает только при включённой настройке.
 * Проверяет токен и принадлежность счёта клиенту.
 * Загружает счёт с блокировкой.
 *
 * Изменение баланса и сохранение операции
 * выполняются в одной транзакции.
 *
 * Не принимает customerId от клиента.
 * Не имитирует взаимодействие с платёжной системой:
 * это явная учебная функция.
 */

import com.example.minibank.account.Account;
import com.example.minibank.account.AccountRepository;
import com.example.minibank.auth.SessionAuthenticationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class DemoDepositService {

    private static final BigDecimal MAX_DEPOSIT =
            new BigDecimal("1000000.00");

    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final SessionAuthenticationService authenticationService;
    private final boolean demoDepositEnabled;

    public DemoDepositService(
            AccountRepository accountRepository,
            OperationRepository operationRepository,
            SessionAuthenticationService authenticationService,
            @Value("${app.demo-deposit-enabled:false}")
            boolean demoDepositEnabled
    ) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        this.authenticationService = authenticationService;
        this.demoDepositEnabled = demoDepositEnabled;
    }

    @Transactional
    public DemoDepositResponse deposit(
            UUID accountId,
            String authorizationHeader,
            DemoDepositRequest request
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        if (!demoDepositEnabled) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Demo deposits are disabled"
            );
        }

        BigDecimal amount = validateAmount(request.amount());

        Account account = accountRepository
                .findOwnedAccountForUpdate(accountId, customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found"
                ));

        if (!"RUB".equals(account.getCurrency())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unsupported account currency"
            );
        }

        BigDecimal newBalance = account.getBalance().add(amount);

        if (newBalance.precision() > 19) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Account balance limit reached"
            );
        }

        account.credit(amount);

        Operation operation = Operation.demoDeposit(
                account.getAccountId(),
                amount
        );

        Operation saved = operationRepository.save(operation);

        return new DemoDepositResponse(
                saved.getOperationId(),
                saved.getOperationType(),
                account.getAccountId(),
                saved.getAmount(),
                saved.getCurrency(),
                account.getBalance(),
                saved.getCreatedAt()
        );
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.signum() <= 0
                || amount.compareTo(MAX_DEPOSIT) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be between 0.01 and 1000000.00"
            );
        }

        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must not contain fractions of a kopeck"
            );
        }
    }
}