# Test Messenger

Тестовое мобильное приложение мессенджера на Android с использованием современного стека технологий и Clean Architecture.

## Технологии

### Архитектура
- **Clean Architecture** - разделение на слои: data, domain, presentation
- **MVI Pattern** - Model-View-Intent для управления состоянием UI
- **SOLID принципы** - следование принципам объектно-ориентированного программирования

### Основной стек
- **Kotlin** - язык программирования
- **Jetpack Compose** - современный UI toolkit
- **Coroutines & Flow** - асинхронное программирование
- **Hilt** - dependency injection
- **Navigation Compose** - навигация между экранами

### Сетевой слой
- **Retrofit** - HTTP клиент
- **OkHttp** - низкоуровневый HTTP клиент с interceptors
- **Kotlinx Serialization** - сериализация JSON
- **JWT** - авторизация с access/refresh токенами

### Локальное хранилище
- **Room** - локальная база данных для кэширования профиля
- **DataStore** - хранение токенов и настроек

### Дополнительно
- **Coil** - загрузка изображений
- **Material Design 3** - дизайн система
- **LibPhoneNumber** - работа с номерами телефонов

## Архитектура проекта

```
app/
├── data/                          # Слой данных
│   ├── local/                     # Локальное хранилище
│   │   ├── dao/                   # Room DAO
│   │   ├── entity/                # Room entities
│   │   ├── datastore/             # DataStore для токенов
│   │   └── AppDatabase.kt
│   ├── remote/                    # Сетевой слой
│   │   ├── api/                   # Retrofit API интерфейсы
│   │   ├── dto/                   # Data Transfer Objects
│   │   └── interceptor/           # OkHttp interceptors (Auth, Token Refresh)
│   └── repository/                # Реализации репозиториев
├── domain/                        # Бизнес-логика
│   ├── model/                     # Domain модели
│   ├── repository/                # Интерфейсы репозиториев
│   └── usecase/                   # Use cases
│       ├── auth/
│       └── profile/
├── presentation/                  # UI слой
│   ├── screens/                   # Экраны приложения
│   │   ├── auth/                  # Авторизация
│   │   │   ├── phone/             # Ввод телефона (MVI)
│   │   │   ├── code/              # Ввод SMS кода (MVI)
│   │   │   └── registration/      # Регистрация (MVI)
│   │   ├── chats/                 # Чаты
│   │   │   ├── list/              # Список чатов (UI only)
│   │   │   └── detail/            # Экран чата (UI only)
│   │   └── profile/               # Профиль
│   │       ├── view/              # Просмотр профиля (MVI)
│   │       └── edit/              # Редактирование (MVI)
│   ├── components/                # Переиспользуемые компоненты
│   ├── navigation/                # Навигация
│   └── theme/                     # Тема приложения
├── di/                            # Dependency Injection модули
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
└── util/                          # Утилиты

```

## Реализованный функционал

### Авторизация
✅ Ввод номера телефона с валидацией
✅ Отправка SMS кода (API: `/api/v1/users/send-auth-code/`)
✅ Проверка кода подтверждения (код для теста: `133337`)
✅ Регистрация нового пользователя
✅ Автоматический refresh токенов при истечении (10 минут)
✅ Сохранение токенов в DataStore

### Профиль
✅ Получение данных пользователя (API: `/api/v1/users/me/`)
✅ Кэширование профиля в Room
✅ Отображение: имя, username, телефон, город, дата рождения, знак зодиака, о себе
✅ Автоматический расчет знака зодиака по дате рождения
✅ Редактирование профиля
✅ Обновление данных на сервере (API: `PUT /api/v1/users/me/`)

### Чаты (UI Only)
✅ Список чатов с mock данными
✅ Экран чата с сообщениями
✅ Поле ввода сообщения (UI only, без отправки)

### Обработка ошибок
✅ Loading состояния для всех запросов
✅ Error handling с отображением Toast
✅ Автоматический retry при 401 с refresh token
✅ Mutex для предотвращения race condition при обновлении токенов

## API

Использован API от **plannerok.ru**:
- Base URL: `https://plannerok.ru`
- Документация: https://plannerok.ru/docs

### Основные эндпоинты:
- `POST /api/v1/users/send-auth-code/` - отправка SMS кода
- `POST /api/v1/users/check-auth-code/` - проверка кода
- `POST /api/v1/users/register/` - регистрация
- `POST /api/v1/users/refresh-token/` - обновление токена
- `GET /api/v1/users/me/` - получение профиля
- `PUT /api/v1/users/me/` - обновление профиля

## Особенности реализации

### MVI Pattern
Каждый экран с бизнес-логикой использует MVI паттерн:
- **State** - неизменяемое состояние UI
- **Event** - действия пользователя
- **Effect** - одноразовые эффекты (навигация, toast)

Пример:
```kotlin
// Contract
data class PhoneState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class PhoneEvent {
    data class OnPhoneChanged(val phone: String) : PhoneEvent()
    data object OnSendCodeClick : PhoneEvent()
}

sealed class PhoneEffect {
    data class NavigateToCode(val phone: String) : PhoneEffect()
}

// ViewModel
@HiltViewModel
class PhoneViewModel @Inject constructor(
    private val sendAuthCodeUseCase: SendAuthCodeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PhoneState())
    val state: StateFlow<PhoneState> = _state.asStateFlow()

    private val _effect = Channel<PhoneEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: PhoneEvent) { /* ... */ }
}
```

### Управление токенами
Реализована автоматическая система обновления токенов:
- `AuthInterceptor` - добавляет access token к запросам
- `TokenRefreshInterceptor` - перехватывает 401 и обновляет токены
- `Mutex` - предотвращает race condition при одновременных запросах

### Кэширование
- Профиль пользователя кэшируется в Room
- При отсутствии сети отображаются кэшированные данные
- Опция `forceRefresh` для принудительного обновления

### Навигация
- Single Activity с Jetpack Navigation Compose
- Автоматическое определение стартового экрана (авторизация/чаты)
- Очистка back stack после успешной авторизации

## Требования

- Android Studio Koala | 2024.1.1 или новее
- Android SDK 24+
- Kotlin 2.0.21
- Gradle 8.13.2

## Сборка проекта

```bash
# Клонирование репозитория
git clone <repository-url>
cd TestMessenger

# Сборка debug версии
./gradlew assembleDebug

# Установка на устройство
./gradlew installDebug
```

## Тестирование

Для тестирования используйте:
- **Телефон**: любой валидный номер в формате +7XXXXXXXXXX
- **SMS код**: `133337` (тестовый код, SMS не приходит)

## Скриншоты

[TODO: Добавить скриншоты после тестирования]

## Автор

Сергей Крайнюков

## Лицензия

Тестовый проект