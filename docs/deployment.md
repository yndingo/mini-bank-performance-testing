# Развёртывание стенда

## Требования
## Сборка образа приложения
## Переменные окружения
## Инициализация PostgreSQL
## Запуск Docker Compose
## Доступ к сервисам
## Проверка работоспособности
## Остановка стенда

| Сервис            | CPU  | Память  |
| ----------------- | ---- | ------- |
| app               | 1    | 1 GiB   |
| db                | 1    | 1 GiB   |
| prometheus        | 1    | 1 GiB   |
| grafana           | 1    | 1 GiB   |
| postgres-exporter | 0,25 | 256 MiB |
| jmeter-proxy      | 0,25 | 64 MiB  |
| cadvisor          | 0,5  | 512 MiB |

Для JVM приложения:
-Xms256m -Xmx512m

- [Compose](../compose.yaml)
- [Java-проект](../Java%20app/mini%20bank/)
- [Конфигурация Prometheus](../monitoring/prometheus/prometheus.yml)
- [Дашборды](../monitoring/grafana/README.md)

