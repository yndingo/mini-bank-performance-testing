# Мониторинг стенда

Мониторинг построен на Prometheus и Grafana.

## Источники метрик

| Компонент | Источник | Prometheus job |
|---|---|---|
| Spring Boot | /actuator/prometheus | mini-bank |
| PostgreSQL | postgres-exporter | postgres |
| Контейнеры | cAdvisor | cadvisor |
| JMeter | Prometheus Backend Listener через проброс порта | jmeter |
| Prometheus | Собственные метрики | prometheus |

## Конфигурация

- [Prometheus](prometheus/prometheus.yml)
- [Docker Compose](../compose.yaml)
- [Дашборды Grafana](grafana/README.md)
- [Подробное описание мониторинга](../docs/monitoring.md)

## Интервалы сбора

- Основной интервал: 15 секунд.
- JMeter: 5 секунд.
- cAdvisor: 5 секунд.

## Важные особенности

- Метрики JMeter доступны во время выполнения теста.
- DOWN target JMeter после остановки теста
  не означает остановку Prometheus.
- Адрес 172.18.0.1 в конфигурации зависит от сети стенда
  и должен быть проверен при развёртывании на другой машине.
- CPU контейнеров измеряется в эквивалентах занятого ядра.
- Working set контейнера не равен Java heap.
- Накопительные счётчики не следует интерпретировать
  как значения только за последний нагрузочный тест.