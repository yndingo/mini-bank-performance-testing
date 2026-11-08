## Переменные окружения

В корне репозитория расположен [.env.example](../.env.example).

На Ubuntu, из каталога с compose.yaml:

```bash
cp .env.example .env
```

Перед запуском проверить значения:

| Переменная | Назначение |
|---|---|
| POSTGRES_DB | Имя базы данных |
| POSTGRES_USER | Пользователь инициализации PostgreSQL |
| POSTGRES_PASSWORD | Пароль пользователя PostgreSQL |
| MINIBANK_DB_PASSWORD | Пароль пользователя приложения minibank_app |
| POSTGRES_EXPORTER_PASSWORD | Пароль пользователя postgres_exporter |

Имена базы и пользователей должны соответствовать compose.yaml
и фактической инициализации PostgreSQL.

## Проверка конфигурации

Из каталога с compose.yaml:

```bash
sudo docker compose config --quiet
```

Команда проверяет корректность конфигурации Compose.
Она не создаёт пользователей БД и не собирает образ приложения.