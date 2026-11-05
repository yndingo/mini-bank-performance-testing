package com.example.minibank.operation;

/*
 * Двухэтапный внутренний перевод.
 *
 * prepare():
 * проверяет владельца и параметры,
 * создаёт PENDING на 5 минут без изменения балансов.
 *
 * confirm():
 * проверяет владельца, блокирует клиента и сессию,
 * выполняет перевод через существующий TransferService.
 *
 * transactionId используется как внутренний ключ
 * идемпотентности при вызове TransferService.
 *
 * Балансы, операция, результат запроса и статус сессии
 * сохраняются в одной транзакции.
 */

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
import java.time.Instant;
import java.util.UUID;

@Service
public class TransferSessionService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransferSessionRepository sessionRepository;
    private final SessionAuthenticationService authenticationService;
    private final TransferService transferService;

    public TransferSessionService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            TransferSessionRepository sessionRepository,
            SessionAuthenticationService authenticationService,
            TransferService transferService
    ) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.sessionRepository = sessionRepository;
        this.authenticationService = authenticationService;
        this.transferService = transferService;
    }

    @Transactional
    public PrepareTransferResponse prepare(
            String authorizationHeader,
            TransferRequest request
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        BigDecimal amount = validateAmount(request.amount());

        UUID sourceId = request.sourceAccountId();

        if (sourceId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source account is required"
            );
        }

        accountRepository.findOwnedAccountId(sourceId, customerId)
                .orElseThrow(TransferSessionService::accountNotFound);

        UUID destinationId = accountRepository
                .findAccountIdByAccountNumber(
                        request.destinationAccountNumber()
                )
                .orElseThrow(TransferSessionService::accountNotFound);

        if (sourceId.equals(destinationId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source and destination accounts must differ"
            );
        }

        TransferSession session = sessionRepository.save(
                new TransferSession(
                        customerId,
                        sourceId,
                        destinationId,
                        amount
                )
        );

        return new PrepareTransferResponse(
                session.getTransactionId(),
                session.getStatus(),
                sourceId,
                request.destinationAccountNumber(),
                session.getAmount(),
                session.getCurrency(),
                session.getExpiresAt()
        );
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConfirmTransferResponse confirm(
            String authorizationHeader,
            UUID transactionId
    ) {
        UUID customerId = authenticationService.requireCustomerId(
                authorizationHeader
        );

        customerRepository.findByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Customer not found"
                ));

        TransferSession session = sessionRepository
                .findOwnedForUpdate(transactionId, customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Transfer not found"
                ));

        if (session.getStatus() == TransferSessionStatus.COMPLETED) {
            return ConfirmTransferResponse.from(session);
        }

        if (!Instant.now().isBefore(session.getExpiresAt())) {
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Transfer confirmation expired"
            );
        }

        String destinationNumber = accountRepository
                .findAccountNumberByAccountId(
                        session.getDestinationAccountId()
                )
                .orElseThrow(TransferSessionService::accountNotFound);

        TransferRequest request = new TransferRequest(
                session.getSourceAccountId(),
                destinationNumber,
                session.getAmount()
        );

        TransferResponse result = transferService.transfer(
                authorizationHeader,
                session.getTransactionId(),
                request
        );

        session.complete(
                result.operationId(),
                result.sourceBalance(),
                result.createdAt()
        );

        return ConfirmTransferResponse.from(session);
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.signum() <= 0
                || amount.compareTo(
                new BigDecimal("1000000.00")
        ) > 0) {
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