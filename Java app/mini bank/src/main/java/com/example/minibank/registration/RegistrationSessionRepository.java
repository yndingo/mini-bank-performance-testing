/*
 * Интерфейс доступа к регистрационным сессиям в БД.
 *
 * Наследует JpaRepository<RegistrationSession, UUID>:
 * - RegistrationSession — тип сохраняемой сущности;
 * - UUID — тип её первичного ключа.
 *
 * Стандартные операции, например save() и findById(),
 * предоставляются Spring Data JPA.
 *
 * findBySessionIdForUpdate() находит сессию с блокировкой
 * PESSIMISTIC_WRITE. Вызывается внутри транзакции сервиса,
 * чтобы параллельные подтверждения не теряли изменения
 * счётчика попыток и не меняли одну сессию одновременно.
 *
 * Не содержит HTTP-логики и правил проверки кода.
 */

package com.example.minibank.registration;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RegistrationSessionRepository
        extends JpaRepository<RegistrationSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select session
            from RegistrationSession session
            where session.sessionId = :sessionId
            """)
    Optional<RegistrationSession> findBySessionIdForUpdate(
            @Param("sessionId") UUID sessionId
    );
}