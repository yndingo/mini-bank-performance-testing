# Нагрузочное тестирование JMeter

## Файлы

- [Тест-план minibank.jmx](minibank.jmx)
- [План тестирования](../docs/test-plan.md)
- [Результаты поиска производительности](../docs/results/capacity-search.md)
- [Подтверждающий тест](../docs/results/confirmation.md)
- [Диагностика проблем](../docs/troubleshooting.md)

## Версия и зависимости

Тест-план сохранён в Apache JMeter 5.4.1.

Используемые компоненты:

- Ultimate Thread Group.
- Prometheus Backend Listener:
  com.github.kolesnikovm.PrometheusListener.
- HTTP implementation: HttpClient4.

| Компонент | Версия |
|---|---|
| Apache JMeter | 5.4.1 |
| Java генератора | Java HotSpot(TM) 64-Bit Server VM (build 17.0.10+11-LTS-240, mixed mode, sharing) |
| Custom Thread Groups / Ultimate Thread Group | version 2.10 |
| Prometheus Backend Listener | jmeter-prometheus-listener-2.6.3 |

## Сценарий

1. GET /api/accounts.
2. Извлечение accountId через JSON Extractor:
   JSONPath $..accountId, первое совпадение.
3. GET /api/accounts/${accountId}/operations.

Оба запроса объединены в Transaction Controller test.
Module Controller вызывает сценарий из отключённой обычной Thread Group.

Проверяется HTTP-код 200 и наличие ожидаемых полей ответа.

Для operations требуется тестовый счёт с непустой историей:
assertion проверяет наличие operationId.

## Авторизация

Используется заранее полученный токен.

Перед запуском локально заполнить переменную Bearer
значением токена без префикса Bearer.

Автоматическое обновление токена не реализовано.
Для 75-минутного теста необходимо заранее проверить
срок действия авторизованной сессии.

Header Manager формирует:

Authorization: Bearer ${Bearer}

Не сохранять действующий токен в публикуемый JMX.

## Соединения

В обоих HTTP Request:

- Implementation: HttpClient4.
- Use KeepAlive: включено.

В локальном `bin/user.properties`:

```properties
httpclient.reset_state_on_thread_group_iteration=false
```

После изменения перезапустить JMeter.

## Мониторинг

Backend Listener экспортирует метрики на порту 9270.

Параметры сохранённого плана:

- testName: mini-bank-read.
- runId: изменить перед каждым запуском.
- exporterPort: 9270.

Доступ Prometheus к генератору организуется через SSH-проброс
и jmeter-proxy. Схема описана
в [документации мониторинга](../docs/monitoring.md).

## Перед запуском

1. Проверить доступность API.
2. Получить актуальный токен.
3. Убедиться, что у счёта есть операции.
4. Задать уникальный runId.
5. Очистить предыдущие результаты.
6. Выбрать новый путь файла JTL.
7. Убедиться, что View Results Tree выключен.
8. Проверить preview расписания Ultimate Thread Group.
9. Запустить тест с работающими таймерами.

## Ограничения

- Один банковский клиентский контекст.
- Нагрузочный сценарий включает только чтение.
- Доступ к приложению проходит через SSH-туннель.
- Transaction Controller не включается в подсчёт HTTP RPS.
- Результаты отдельных ступеней анализируются по времени плато.