# DevQualify — Android

Мобильное приложение для оценки квалификации разработчиков. Позволяет проходить тесты по различным технологиям и направлениям, отслеживать прогресс, читать статьи и участвовать в рейтинге.

## Технологический стек

- **Язык:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Архитектура:** MVVM + Repository pattern
- **DI:** Hilt
- **Сеть:** Retrofit + OkHttp (REST/JSON через API Gateway)
- **Навигация:** Jetpack Navigation Compose
- **Асинхронность:** Kotlin Coroutines + Flow
- **Сериализация:** Gson
- **Min SDK:** 29 (Android 10)
- **Target SDK:** 36
- **JDK:** 21

## Архитектура

```
UI (Compose Screens)
  ↓
ViewModels (State + Business Logic)
  ↓
Repositories (Interfaces)
  ↓
Repository Implementations → Retrofit API → API Gateway (:8080) → gRPC Microservices
```

Приложение взаимодействует с бэкендом через единый API Gateway по REST/HTTP. Gateway проксирует запросы к 4 микросервисам:

| Сервис          | Функциональность                                    |
|-----------------|-----------------------------------------------------|
| Auth Service    | Регистрация, авторизация, подтверждение email        |
| UserInfo Service| Профиль, аватар, достижения, история, лидерборд     |
| Tests Service   | Тесты, сессии, вопросы, результаты                   |
| Articles Service| Статьи, рекомендации, технологии, предпочтения       |

## Структура проекта

```
app/src/main/java/com/diploma/work/
├── data/
│   ├── api/              # Retrofit интерфейсы и DTO
│   │   ├── dto/           # Data Transfer Objects (JSON ↔ Model)
│   │   ├── AuthApi.kt
│   │   ├── UserInfoApi.kt
│   │   ├── TestsApi.kt
│   │   ├── ArticlesApi.kt
│   │   ├── AuthInterceptor.kt
│   │   ├── SafeApiCall.kt
│   │   └── GsonAdapters.kt
│   ├── models/            # Domain модели
│   ├── repository/        # Интерфейсы и реализации репозиториев
│   ├── cache/             # Кэширование статей
│   ├── preferences/       # Пользовательские настройки
│   └── security/          # Токены, аватары
├── di/                    # Hilt модули (AppModule)
├── ui/
│   └── feature/           # Экраны приложения
│       ├── auth/          # Авторизация и регистрация
│       ├── home/          # Главный экран
│       ├── test/          # Прохождение тестов
│       ├── articles/      # Лента статей
│       ├── profile/       # Профиль пользователя
│       ├── leaderboard/   # Рейтинг
│       ├── achievements/  # Достижения
│       ├── history/       # История тестов
│       ├── interests/     # Интересы и предпочтения
│       └── feedback/      # Обратная связь
└── utils/                 # Утилиты, константы, обработка ошибок
```

## Настройка и запуск

### Предварительные требования

- Android Studio Ladybug (2024.2) или новее
- JDK 21
- Запущенный API Gateway (по умолчанию порт 8080)

### Конфигурация

1. Скопируйте файл-пример конфигурации:

```bash
cp app/src/main/java/com/diploma/work/utils/Constants.kt.example \
   app/src/main/java/com/diploma/work/utils/Constants.kt
```

2. Откройте `Constants.kt` и укажите IP-адрес сервера с API Gateway:

```kotlin
const val GATEWAY_BASE_URL = "http://<YOUR_SERVER_IP>:8080/api/v1/"
```

3. Для работы на эмуляторе с локальным сервером используйте `10.0.2.2` вместо `localhost`:

```kotlin
const val GATEWAY_BASE_URL = "http://10.0.2.2:8080/api/v1/"
```

### Сборка

```bash
./gradlew assembleDebug
```

### Запуск тестов

```bash
./gradlew test
```

## Функциональность

- **Авторизация** — регистрация, вход, подтверждение email
- **Тестирование** — прохождение тестов с таймером, автосохранение прогресса, возобновление прерванных сессий
- **Статьи** — персонализированная лента, поиск, рекомендации
- **Профиль** — настройка направления и уровня, загрузка аватара
- **Достижения** — отслеживание прогресса
- **Лидерборд** — рейтинг пользователей с фильтрацией по направлению и уровню
- **Обратная связь** — отправка отзывов
