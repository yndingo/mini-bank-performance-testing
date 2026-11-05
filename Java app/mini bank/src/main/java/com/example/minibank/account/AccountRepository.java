package com.example.minibank.account;

/*
 * Репозиторий банковских счетов.
 *
 * JpaRepository предоставляет сохранение и поиск по accountId.
 * Дополнительные методы позволяют:
 * - получить счета конкретного клиента;
 * - найти счёт по его учебному номеру.
 *
 * Не выполняет переводы и не определяет права доступа.
 * Эти проверки будут в сервисах.
 */

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    List<Account> findAllByCustomerId(UUID customerId);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select account
        from Account account
        where account.accountId = :accountId
          and account.customerId = :customerId
        """)
    Optional<Account> findOwnedAccountForUpdate(
            @Param("accountId") UUID accountId,
            @Param("customerId") UUID customerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select account
        from Account account
        where account.accountId = :accountId
        """)
    Optional<Account> findByAccountIdForUpdate(
            @Param("accountId") UUID accountId
    );

    @Query("""
        select account.accountId
        from Account account
        where account.accountNumber = :accountNumber
        """)
    Optional<UUID> findAccountIdByAccountNumber(
            @Param("accountNumber") String accountNumber
    );

    @Query("""
        select account.accountId
        from Account account
        where account.accountId = :accountId
          and account.customerId = :customerId
        """)
    Optional<UUID> findOwnedAccountId(
            @Param("accountId") UUID accountId,
            @Param("customerId") UUID customerId
    );

    @Query("""
        select account.accountNumber
        from Account account
        where account.accountId = :accountId
        """)
    Optional<String> findAccountNumberByAccountId(
            @Param("accountId") UUID accountId
    );

}