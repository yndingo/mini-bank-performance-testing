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

Для запросов используется `Authorization: Bearer <token>`.

## Бизнес-операции

Приложение реализует многошаговые операции.
Последующие этапы используют идентификатор, полученный
на предыдущем этапе.

Все пути ниже указаны относительно базового адреса приложения.
Для текущего стенда: http://127.0.0.1:8080.

### Регистрация — три этапа

| Этап | Метод и путь | Назначение | Успешный HTTP-код |
|---|---|---|---:|
| 1 | POST /api/registrations | Начало регистрации | 201 |
| 2 | POST /api/registrations/{sessionId}/confirm-phone | Подтверждение телефона кодом | 200 |
| 3 | POST /api/registrations/{sessionId}/complete | Завершение регистрации | 201 |

Связующий идентификатор — sessionId регистрационной сессии.

На первом этапе передаются ФИО, телефон и пароль.
На втором этапе передаётся код подтверждения.
Третий этап не требует повторной передачи регистрационных данных.

При завершении создаются клиент, счёт и учебная карта.
Создание выполняется одной транзакцией.
Ответ содержит идентификаторы клиента, счёта и карты,
номер счёта, валюту, баланс, номер карты и срок её действия.

Исходный код:
- [RegistrationController](mini%20bank/src/main/java/com/example/minibank/registration/RegistrationController.java)
- [RegistrationCompletionService](mini%20bank/src/main/java/com/example/minibank/registration/RegistrationCompletionService.java)

### Авторизация — два этапа

| Этап | Метод и путь | Назначение | Успешный HTTP-код |
|---|---|---|---:|
| 1 | POST /api/auth/login | Начало входа по телефону и паролю | 201 |
| 2 | POST /api/auth/login/{sessionId}/confirm | Подтверждение входа кодом | 200 |

Связующий идентификатор — sessionId сессии авторизации.

После подтверждения клиент получает токен для дальнейших запросов.
Защищённые операции используют заголовок
`Authorization: Bearer <token>`.

Выход выполняется отдельным запросом:

POST /api/auth/logout

Успешный ответ — HTTP 204 без тела.

Исходный код:
- [AuthController](mini%20bank/src/main/java/com/example/minibank/auth/AuthController.java)

### Внутренний перевод — два этапа

| Этап | Метод и путь | Назначение | Успешный HTTP-код |
|---|---|---|---:|
| 1 | POST /api/transfers/prepare | Подготовка перевода | 201 |
| 2 | POST /api/transfers/{transactionId}/confirm | Подтверждение подготовленного перевода | 200 |

Связующий идентификатор — transactionId.

В prepare передаются:
- sourceAccountId — идентификатор счёта отправителя;
- destinationAccountNumber — номер счёта получателя;
- amount — сумма.

Оба этапа требуют Bearer-токен владельца.
Подтверждение использует transactionId; реквизиты перевода
повторно не передаются.

Исходный код:
- [TransferController](mini%20bank/src/main/java/com/example/minibank/operation/TransferController.java)

### Внешний перевод — два этапа

| Этап | Метод и путь | Назначение | Успешный HTTP-код |
|---|---|---|---:|
| 1 | POST /api/external-transfers/prepare | Подготовка внешнего перевода | 201 |
| 2 | POST /api/external-transfers/{transactionId}/confirm | Подтверждение подготовленного перевода | 200 |

Связующий идентификатор — transactionId.

В prepare передаются:
- sourceAccountId;
- recipientName;
- recipientAccount;
- recipientBankCode;
- amount.

Оба этапа требуют Bearer-токен владельца.
Confirm не принимает реквизиты повторно.

Исходный код:
- [ExternalTransferController](mini%20bank/src/main/java/com/example/minibank/operation/ExternalTransferController.java)

### Платёж по шаблону

Предварительно создаётся шаблон:

POST /api/payment-templates

Успешный ответ — HTTP 201.

Список шаблонов текущего клиента:

GET /api/payment-templates

Успешный ответ — HTTP 200.

Сам платёж выполняется в два этапа:

| Этап | Метод и путь | Назначение | Успешный HTTP-код |
|---|---|---|---:|
| 1 | POST /api/payments/prepare | Подготовка платежа по шаблону | 201 |
| 2 | POST /api/payments/{transactionId}/confirm | Подтверждение платежа | 200 |

В prepare передаются:
- templateId — идентификатор шаблона;
- sourceAccountId — идентификатор счёта списания;
- amount — сумма.

templateId выбирает шаблон.
transactionId связывает подготовку и подтверждение конкретного платежа.

Все перечисленные запросы требуют Bearer-токен.
Подтверждение использует сохранённые параметры платежа.

Платёж является учебной имитацией:
реальная платёжная система не вызывается.
В ответе используется признак simulated=true.

Исходный код:
- [PaymentTemplateController](mini%20bank/src/main/java/com/example/minibank/payment/PaymentTemplateController.java)
- [PaymentController](mini%20bank/src/main/java/com/example/minibank/payment/PaymentController.java)
- [PaymentResponse](mini%20bank/src/main/java/com/example/minibank/payment/PaymentResponse.java)

## Связь с тестированием

Примеры запросов:
[Postman-коллекция](../postman/README.md).

Текущий нагрузочный сценарий включает только чтение счетов
и истории операций. Регистрация, авторизация, переводы
и платежи в его нагрузочный профиль не включены.

Описание:
[JMeter](../jmeter/README.md).