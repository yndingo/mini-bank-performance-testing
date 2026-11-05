package com.example.minibank.card;

/*
 * Репозиторий учебных карт.
 *
 * Предоставляет стандартные операции сохранения и поиска по UUID.
 * findAllByAccountId() возвращает карты указанного счёта.
 *
 * Не проверяет, принадлежит ли счёт текущему пользователю:
 * эту проверку должен выполнять сервис перед выдачей данных.
 */

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findAllByAccountId(UUID accountId);
}