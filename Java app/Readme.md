# Java-приложение Mini-bank

Приложение мини-банка на Java Spring Boot с PostgreSQL.

## Исходный код

- [Корень Java-проекта](mini%20bank/)
- [Maven-конфигурация](mini%20bank/pom.xml)
- [Исходные файлы](mini%20bank/src/)

## Проверка и запуск

- [Развёртывание Docker-стенда](../docs/deployment.md)
- [Postman-коллекция](../postman/README.md)
- [Мониторинг приложения](../docs/monitoring.md)

## Проверенный сценарий чтения

1. Получение счетов клиента: GET /api/accounts.
2. Извлечение accountId из ответа.
3. Получение операций:
   GET /api/accounts/${accountId}/operations.

Для запросов используется Authorization: Bearer <token>.

## Функциональные возможности

Описание регистрации, авторизации, переводов и платежей
добавляется по фактическим контроллерам и контрактам API.