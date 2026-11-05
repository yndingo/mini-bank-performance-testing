package com.example.minibank.operation;

/*
 * Репозиторий операций.
 *
 * findAccountHistory() находит операции,
 * где счёт участвовал в списании или зачислении.
 *
 * Возвращает новые операции первыми.
 * Pageable ограничивает размер выдачи.
 *
 * Принадлежность счёта проверяется в сервисе.
 */

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OperationRepository
        extends JpaRepository<Operation, UUID> {

    @Query("""
            select operation
            from Operation operation
            where operation.sourceAccountId = :accountId
               or operation.destinationAccountId = :accountId
            order by operation.createdAt desc,
                     operation.operationId desc
            """)
    List<Operation> findAccountHistory(
            @Param("accountId") UUID accountId,
            Pageable pageable
    );
}