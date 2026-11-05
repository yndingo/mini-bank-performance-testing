package com.example.minibank.operation;

/*
 * Сервис переводов с идемпотентностью.
 *
 * Порядок обработки:
 * 1. Проверка токена и суммы.
 * 2. Блокировка клиента.
 * 3. Поиск результата по клиенту и ключу.
 * 4. Возврат сохранённого результата либо новый перевод.
 *
 * Новый перевод:
 * - проверка владельца;
 * - блокировка счетов в едином порядке;
 * - списание и зачисление;
 * - сохранение операции и результата запроса.
 *
 * Все изменения выполняются в одной транзакции.
 * Неуспешный запрос ключ не занимает.
 */

import com.example.minibank.account.Account;
import com.example.minibank.account.AccountRepository;
import com.example.minibank.auth.SessionAuthenticationService;
import com.example.minibank.customer.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferService {

    private static final BigDecimal MAX_TRANSFER =
            new BigDecimal("1000000.00");

    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;
    private final SessionAuthenticationService authenticationService;
    private final CustomerRepository customerRepository;
    private final TransferRequestRepository transferRequestRepository;

    public TransferService(
            AccountRepository accountRepository,
            OperationRepository operationRepository,
            SessionAuthenticationService authenticationService,
            CustomerRepository customerRepository,
            TransferRequestRepository transferRequestRepository
    ) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        this.authenticationService = authenticationService;
        this.customerRepository = customerRepository;
        this.transferRequestRepository = transferRequestRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransferResponse transfer(
            String authorizationHeader,
            UUID idempotencyKey,
            TransferRequest request
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        if (idempotencyKey == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Idempotency key is required"
            );
        }

        BigDecimal amount = validateAmount(request.amount());

        UUID sourceId = request.sourceAccountId();

        if (sourceId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source account is required"
            );
        }

        customerRepository.findByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Customer not found"
                ));

        TransferRequestId requestId = new TransferRequestId(
                customerId,
                idempotencyKey
        );

        Optional<TransferRequestRecord> previous =
                transferRequestRepository.findById(requestId);

        if (previous.isPresent()) {
            TransferRequestRecord savedRequest = previous.get();

            if (!savedRequest.matches(request, amount)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency key was used with different parameters"
                );
            }

            return savedRequest.toResponse();
        }

        accountRepository.findOwnedAccountId(sourceId, customerId)
                .orElseThrow(TransferService::accountNotFound);

        UUID destinationId = accountRepository
                .findAccountIdByAccountNumber(
                        request.destinationAccountNumber()
                )
                .orElseThrow(TransferService::accountNotFound);

        if (sourceId.equals(destinationId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source and destination accounts must differ"
            );
        }

        UUID firstId = sourceId.compareTo(destinationId) < 0
                ? sourceId : destinationId;

        UUID secondId = firstId.equals(sourceId)
                ? destinationId : sourceId;

        Account first = lockAccount(firstId);
        Account second = lockAccount(secondId);

        Account source = first.getAccountId().equals(sourceId)
                ? first : second;

        Account destination = first.getAccountId().equals(destinationId)
                ? first : second;

        if (!source.getCustomerId().equals(customerId)) {
            throw accountNotFound();
        }

        if (!"RUB".equals(source.getCurrency())
                || !"RUB".equals(destination.getCurrency())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unsupported account currency"
            );
        }

        if (source.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient funds"
            );
        }

        BigDecimal destinationBalance =
                destination.getBalance().add(amount);

        if (destinationBalance.precision() > 19) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Destination balance limit reached"
            );
        }

        source.debit(amount);
        destination.credit(amount);

        Operation saved = operationRepository.save(
                Operation.transfer(sourceId, destinationId, amount)
        );

        TransferResponse response = new TransferResponse(
                saved.getOperationId(),
                saved.getOperationType(),
                sourceId,
                destination.getAccountNumber(),
                saved.getAmount(),
                saved.getCurrency(),
                source.getBalance(),
                saved.getCreatedAt()
        );

        transferRequestRepository.save(
                new TransferRequestRecord(
                        customerId,
                        idempotencyKey,
                        response
                )
        );

        return response;
    }

    private Account lockAccount(UUID accountId) {
        return accountRepository.findByAccountIdForUpdate(accountId)
                .orElseThrow(TransferService::accountNotFound);
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.signum() <= 0
                || amount.compareTo(MAX_TRANSFER) > 0) {
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

    private static ResponseStatusException accountNotFound() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Account not found"
        );
    }
}