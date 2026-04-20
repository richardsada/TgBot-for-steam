# Steam Helper Bot

Telegram-бот для просмотра статистики аккаунта Steam с поддержкой AI-аналитики.

## Описание

Steam Helper Bot — это Telegram-бот, разработанный на Java с использованием Spring Boot, который позволяет пользователям привязывать свой аккаунт Steam и просматривать подробную статистику игр. Бот также предоставляет AI-аналитику игрового профиля через интегрированный AI-сервис.

## Возможности

- **Привязка аккаунта Steam** — пользователи могут привязать свой аккаунт Steam к Telegram-аккаунту
- **Просмотр статистики** — отображение информации об аккаунте: никнейм, количество игр, общее время в играх, уровень Steam, количество друзей и т.д.
- **AI-аналитика** — получение автоматически сгенерированного описания игрового профиля от AI-сервиса
- **Отвязка аккаунта** — возможность отвязать привязанный аккаунт Steam

## Технологический стек

- **Java 21** — основной язык программирования
- **Spring Boot 4.0.3** — фреймворк для создания приложения
- **Telegram Bots API 6.5.0** — библиотека для работы с Telegram Bot API
- **Maven** — система сборки проекта
- **Lombok 1.18.44** — библиотека для сокращения boilerplate-кода
- **Docker** — контейнеризация приложения

## Требования

- Java 21 или выше
- Maven 3.9+ (илиWrapper в проекте)
- Docker (для контейнеризации)
- Токен Telegram-бота (получить у @BotFather)
- Доступ к внешним сервисам (Auth Service, Steam Stats Service, AI Service)

## Установка и запуск

### 1. Клонирование репозитория

```bash
<<<<<<< HEAD
git clone https://github.com/richardsada/TgBot-for-steam
=======
git clone <_repository_url>
>>>>>>> e454745 (-added-readme)
cd bot-service
```

### 2. Настройка переменных окружения

Создайте файл `.env` или экспортируйте переменные окружения:

```bash
export TG_BOT_TOKEN="your_telegram_bot_token"
export TG_BOT_NAME="your_bot_username"
export AUTH_SERVICE_URL="http://localhost:8080"
export STEAM_SERVICE_URL="http://localhost:8082"
export AI_SERVICE_URL="http://localhost:8083"
```

### 3. Сборка проекта

```bash
./mvnw clean package -DskipTests
```

### 4. Запуск приложения

```bash
java -jar target/bot-1.0.jar
```

Или используя Maven:

```bash
./mvnw spring-boot:run
```

## Docker

### Сборка Docker-образа

```bash
docker build -t steam-helper-bot .
```

### Запуск контейнера

```bash
docker run -d \
  -e TG_BOT_TOKEN="your_telegram_bot_token" \
  -e TG_BOT_NAME="your_bot_username" \
  -e AUTH_SERVICE_URL="http://auth-service:8080" \
  -e STEAM_SERVICE_URL="http://steam-service:8082" \
  -e AI_SERVICE_URL="http://ai-service:8083" \
  --name steam-helper-bot \
  steam-helper-bot
```

## Доступные команды бота

| Команда | Описание |
|---------|----------|
| `/start` | Начать работу с ботом |
| `/help` | Показать список команд и помощь |
| `/bind <ссылка>` | Привязать аккаунт Steam |
| `/unbind` | Отвязать привязанный аккаунт Steam |
| `/stats` | Показать статистику по играм |
| `/ai` | Показать описание от AI |

### Пример использования

1. Запуск бота:
   ```
   /start
   ```

2. Привязка аккаунта Steam:
   ```
   /bind https://steamcommunity.com/id/username
   ```

3. Просмотр статистики:
   ```
   /stats
   ```

4. Просмотр AI-аналитики:
   ```
   /ai
   ```

## Конфигурация

Параметры конфигурации находятся в файле [`src/main/resources/application.properties`](src/main/resources/application.properties):

```properties
# Имя приложения
spring.application.name=Tg_bot

# Порт сервера
server.port=8081

# Токен Telegram-бота
telegram.bot.token=${TG_BOT_TOKEN}

# Имя Telegram-бота
telegram.bot.username=${TG_BOT_NAME}

# Уровень логирования
logging.level=${LOGGING_LEVEL:INFO}

# URL сервиса авторизации
auth.service.url=${AUTH_SERVICE_URL}

# URL сервиса статистики Steam
steam.stats.service.url=${STEAM_SERVICE_URL}

# URL AI-сервиса
ai.stats.service.url=${AI_SERVICE_URL}
```

## Архитектура проекта

```
bot-service/
├── src/
│   ├── main/
│   │   ├── java/com/Gdz/bot/
│   │   │   ├── TgBotApplication.java          # Главный класс приложения
│   │   │   ├── SteamHelperBot.java             # Основной класс Telegram-бота
│   │   │   ├── config/
│   │   │   │   └── TelegramBotConfig.java     # Конфигурация бота
│   │   │   ├── dto/
│   │   │   │   └── SteamStatsDto.java         # DTO для статистики Steam
│   │   │   └── service/
│   │   │       ├── AuthService.java             # Сервис авторизации
│   │   │       ├── SteamService.java          # Сервис статистики Steam
│   │   │       └── AiService.java            # AI-сервис
│   │   └── resources/
│   │       └── application.properties         # Конфигурация приложения
│   └── test/
│       └── java/com/Gdz/bot/
│           └── (unit tests)
├── Dockerfile                                # Docker-образ
├── pom.xml                                   # Maven-зависимости
└── mvnw, mvnw.cmd                           # Maven Wrapper
```

## Внешние сервисы

Бот взаимодействует со следующими внешними сервисами:

1. **Auth Service** — отве��ает за привязку и отвязку аккаунтов Steam к Telegram-аккаунтам
2. **Steam Stats Service** — предоставляет статистику аккаунта Steam
3. **AI Service** — генерирует описание игрового профиля на основе данных

### API Endpoints

#### Auth Service
- `POST /bind` — привязка аккаунта Steam
- `DELETE /link/{telegramId}` — отвязка аккаунта Steam

#### Steam Stats Service
- `GET /stats/{telegramId}` — получение статистики Steam

#### AI Service
- `GET /api/ai/summaries/{telegramId}` — получение AI-аналитики

## Логирование

Приложение использует SLF4J для логирования. Уровень логирования можно настроить через переменную окружения `LOGGING_LEVEL`:

```bash
export LOGGING_LEVEL=DEBUG
```

## Тестирование

Запуск тестов:

```bash
./mvnw test
```
