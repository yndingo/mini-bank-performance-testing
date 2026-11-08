# Дашборды Grafana

## Дашборды в репозитории

| Файл | Назначение |
|---|---|
| [jvm.json](dashboards/jvm.json) | JVM, CPU процесса, память, GC и HTTP-метрики приложения |
| [postgres.json](dashboards/postgres.json) | PostgreSQL: сессии, транзакции, блокировки и checkpoint |
| [load-testing.json](dashboards/load-testing.json) | Метрики JMeter и ресурсы контейнеров |

## Импорт

1. Открыть Dashboards → Import.
2. Загрузить JSON нужного дашборда.
3. Выбрать datasource Prometheus, если это предлагается.
4. Проверить переменные и запросы панелей.
5. Выбрать временной диапазон нужного теста.

## Изменения относительно исходных дашбордов

- Добавлены или адаптированы HTTP-метрики приложения.
- Добавлены активные потоки, HTTP RPS, ошибки и p95 JMeter.
- Добавлены CPU и working set контейнеров app и db.
- Добавлены доля периодов и время CPU-троттлинга.
- Адаптирована панель сессий PostgreSQL по состояниям.
- Адаптирована статистика checkpoint PostgreSQL 17
  под pg_stat_checkpointer.

## Интерпретация

- Transaction Controller JMeter не является отдельным HTTP-запросом.
- HTTP RPS считается по HTTP-сэмплерам, без транзакции test.
- CPU 1.0 соответствует эквиваленту одного занятого ядра.
- Доля периодов с троттлингом не равна доле потерянной производительности.
- Среднее значение точек графика p95 не является
  общим p95 запросов за весь выбранный интервал.

## Источники дашбордов

| Локальный файл | Исходный дашборд | Grafana ID |
|---|---|---:|
| [jvm.json](dashboards/jvm.json) | [JVM (Micrometer)](https://grafana.com/grafana/dashboards/4701-jvm-micrometer/) | 4701 |
| [postgres.json](dashboards/postgres.json) | [PostgreSQL Database](https://grafana.com/grafana/dashboards/9628-postgresql-database/) | 9628 |
| [load-testing.json](dashboards/load-testing.json) | Собственный дашборд проекта | — |

Импортированные дашборды адаптированы под метрики и метки стенда.
В репозитории сохранены используемые локальные версии JSON.

Revision исходных импортированных версий не зафиксирован.