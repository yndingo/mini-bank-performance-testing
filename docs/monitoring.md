# Устройство мониторинга

## Поток метрик

- Spring Boot Actuator → Prometheus → Grafana.
- PostgreSQL → postgres-exporter → Prometheus → Grafana.
- Контейнеры → cAdvisor → Prometheus → Grafana.
- JMeter Backend Listener → SSH-проброс → jmeter-proxy →
  Prometheus → Grafana.

## Конфигурация

- [Prometheus](../monitoring/prometheus.yml)
- [Compose](../compose.yaml)
- [Дашборды](../monitoring/grafana/README.md)

## Targets

| Job | Target |
|---|---|
| mini-bank | app:8080/actuator/prometheus |
| postgres | postgres-exporter:9187/metrics |
| cadvisor | cadvisor:8080/metrics |
| jmeter | 172.18.0.1:9271/metrics |
| prometheus | localhost:9090/metrics |

## Доступ к метрикам JMeter

В сохранённом JMX exporterPort равен 9270.

На Ubuntu jmeter-proxy слушает 172.18.0.1:9271
и перенаправляет запросы на 127.0.0.1:9270.

Доставку запросов с Ubuntu:9270 к генератору
обеспечивает SSH-проброс. Доступ к экспортёру генератора обеспечивается SSH-пробросом.
Команды и порядок открытия туннелей приведены
в [инструкции развёртывания](deployment.md#доступ-к-сервисам).

## Интерпретация метрик

- CPU контейнера измеряется в эквивалентах занятого ядра.
- Лимит приложения — 1 CPU.
- Working set не равен Java heap.
- Доля периодов с троттлингом не является процентом
  потерянной производительности.
- Блокировки PostgreSQL нужно отличать от ожидания блокировок.
- Checkpoint write/sync time показаны как скорость
  накопления времени в ms/s.