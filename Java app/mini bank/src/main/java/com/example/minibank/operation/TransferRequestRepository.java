package com.example.minibank.operation;

/*
 * Репозиторий результатов запросов перевода.
 *
 * findById() ищет результат по паре:
 * customerId + idempotencyKey.
 *
 * save() сохраняет результат успешного перевода.
 *
 * Последовательность обработки обеспечивает сервис:
 * сначала блокировка клиента, затем поиск ключа.
 */

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRequestRepository
        extends JpaRepository<
        TransferRequestRecord,
        TransferRequestId
        > {
}