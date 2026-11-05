package com.example.minibank.customer;

/*
 * Репозиторий клиентов.
 *
 * Дополнительно предоставляет findByCustomerIdForUpdate():
 * загрузку клиента с блокировкой для последовательной
 * обработки его запросов перевода.
 *
 * Метод с блокировкой вызывается только внутри транзакции.
 * Проверка токена выполняется отдельно в сервисе.
 */

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository
        extends JpaRepository<Customer, UUID> {

    boolean existsByPhone(String phone);

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByRegistrationSessionId(
            UUID registrationSessionId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select customer
            from Customer customer
            where customer.customerId = :customerId
            """)
    Optional<Customer> findByCustomerIdForUpdate(
            @Param("customerId") UUID customerId
    );
}