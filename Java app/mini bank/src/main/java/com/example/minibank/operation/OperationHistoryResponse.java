package com.example.minibank.operation;

/*
 * DTO операции в истории конкретного счёта.
 *
 * direction:
 * IN — зачисление на просматриваемый счёт;
 * OUT — списание с просматриваемого счёта.
 *
 * amount всегда положительная.
 * Направление определяется относительно текущего счёта.
 */

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OperationHistoryResponse(
        UUID operationId,
        OperationType operationType,
        String direction,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {

    public static OperationHistoryResponse from(
            Operation operation,
            UUID accountId
    ) {
        String direction = accountId.equals(
                operation.getDestinationAccountId()
        ) ? "IN" : "OUT";

        return new OperationHistoryResponse(
                operation.getOperationId(),
                operation.getOperationType(),
                direction,
                operation.getSourceAccountId(),
                operation.getDestinationAccountId(),
                operation.getAmount(),
                operation.getCurrency(),
                operation.getCreatedAt()
        );
    }
}