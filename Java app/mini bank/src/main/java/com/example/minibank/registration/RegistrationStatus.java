/*
 * Статусы многошаговой регистрации.
 *
 * PENDING — регистрационная сессия создана, телефон ещё не подтверждён.
 * PHONE_CONFIRMED — код проверен, телефон подтверждён.
 * COMPLETED — регистрация завершена: клиент, счёт и карта созданы.
 *
 * Используется в сущности RegistrationSession и ответах API.
 * В БД сохраняется строковое имя статуса, например PENDING.
 *
 * На текущем этапе реализован переход PENDING → PHONE_CONFIRMED.
 * Переход в COMPLETED будет добавлен при завершении регистрации.
 */

package com.example.minibank.registration;

public enum RegistrationStatus {
    PENDING,
    PHONE_CONFIRMED,
    COMPLETED
}
