# Архитектура приложения

## Обзор

Приложение построено по принципам **Clean Architecture** с использованием паттерна **MVI** (Model-View-Intent) для управления состоянием UI.

## Слои архитектуры

### 1. Presentation Layer (UI)

**Ответственность**: Отображение данных и обработка взаимодействия с пользователем.

**Компоненты**:
- **Screen** (Composable функции) - UI отображение
- **ViewModel** - управление состоянием, обработка событий
- **Contract** (State/Event/Effect) - контракт между View и ViewModel

**Паттерн MVI**:
```kotlin
// State - неизменяемое состояние UI
data class PhoneState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

// Event - события от пользователя
sealed class PhoneEvent {
    data class OnPhoneChanged(val phone: String) : PhoneEvent()
    data object OnSendCodeClick : PhoneEvent()
}

// Effect - одноразовые эффекты (навигация, toast)
sealed class PhoneEffect {
    data class NavigateToCode(val phone: String) : PhoneEffect()
}
```

**Поток данных**:
1. User действие → Event
2. ViewModel обрабатывает Event
3. ViewModel обновляет State
4. UI реагирует на State изменения
5. ViewModel отправляет Effect (опционально)
6. UI обрабатывает Effect

### 2. Domain Layer (Бизнес-логика)

**Ответственность**: Бизнес-логика приложения, независимая от фреймворков.

**Компоненты**:
- **Model** - доменные модели (User, Chat, Message)
- **Repository Interface** - контракты для получения данных
- **Use Case** - изолированные бизнес-операции

**Принципы**:
- Не зависит от Android SDK
- Не знает о деталях реализации (API, Database)
- Содержит чистую бизнес-логику (например, расчет знака зодиака)

**Пример Use Case**:
```kotlin
class SendAuthCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        return authRepository.sendAuthCode(phone)
    }
}
```

### 3. Data Layer (Данные)

**Ответственность**: Управление данными из разных источников.

**Компоненты**:

#### Remote (Сеть)
- **API** - Retrofit интерфейсы
- **DTO** - Data Transfer Objects
- **Interceptors** - обработка запросов/ответов

#### Local (Локальное хранилище)
- **DAO** - Room Database Access Objects
- **Entity** - Room entities
- **DataStore** - хранение токенов и настроек

#### Repository Implementation
- Координирует Remote и Local источники
- Реализует интерфейсы из Domain слоя
- Конвертирует DTO → Domain models

**Пример Repository**:
```kotlin
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val tokenDataStore: TokenDataStore
) : UserRepository {

    override suspend fun getCurrentUser(forceRefresh: Boolean): Result<User> {
        return runCatchingResult {
            // Сначала пробуем взять из кэша
            if (!forceRefresh) {
                val cachedUser = userDao.getUserById(userId).first()
                if (cachedUser != null) return@runCatchingResult cachedUser.toDomain()
            }

            // Загружаем с сервера
            val response = userApi.getCurrentUser()
            val user = response.profileData.toDomain()

            // Сохраняем в кэш
            userDao.insertUser(user.toEntity())

            user
        }
    }
}
```

## Dependency Injection (Hilt)

**Модули**:

### NetworkModule
- Retrofit
- OkHttp с interceptors
- API интерфейсы
- JSON serializer

### DatabaseModule
- Room Database
- DAO

### RepositoryModule
- Связывает интерфейсы и реализации репозиториев

## Управление состоянием

### StateFlow для UI State
```kotlin
private val _state = MutableStateFlow(PhoneState())
val state: StateFlow<PhoneState> = _state.asStateFlow()
```

### Channel для Effects
```kotlin
private val _effect = Channel<PhoneEffect>()
val effect = _effect.receiveAsFlow()
```

Effects используются для одноразовых событий:
- Навигация
- Toast сообщения
- Запуск анимаций

## Обработка ошибок

### Result Wrapper
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String?) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
```

**Преимущества**:
- Явная обработка ошибок
- Type-safe
- Loading состояния

## JWT Авторизация

### Interceptor Chain
1. **AuthInterceptor** - добавляет access token к запросам
2. **TokenRefreshInterceptor** - обрабатывает 401 и обновляет токены

### Token Refresh Flow
```
Request → AuthInterceptor (add token) → API
       ← 401 Unauthorized ←
       → TokenRefreshInterceptor → Refresh Token API
       ← New tokens ←
       → Retry original request with new token
```

**Особенности**:
- `Mutex` для предотвращения race condition
- Автоматический retry после refresh
- Graceful fallback при ошибке refresh

## Навигация

**Navigation Compose** с single activity pattern:

```kotlin
NavHost(navController, startDestination) {
    composable(Screen.Phone.route) { PhoneScreen() }
    composable(Screen.Code.route) { CodeScreen() }
    // ...
}
```

**Стартовый экран** определяется динамически:
- Если пользователь авторизован → Список чатов
- Если нет → Экран ввода телефона

## SOLID Принципы

### Single Responsibility (SRP)
- Каждый класс имеет одну ответственность
- ViewModel отвечает только за UI state
- Repository только за данные
- Use Case только за одну бизнес-операцию

### Open/Closed (OCP)
- Использование sealed classes для State/Event/Effect
- Расширяемость через интерфейсы

### Liskov Substitution (LSP)
- Repository реализации взаимозаменяемы
- Можно легко заменить на mock для тестов

### Interface Segregation (ISP)
- Маленькие специфичные интерфейсы
- Репозитории разделены по функциональности (Auth, User)

### Dependency Inversion (DIP)
- Зависимости направлены к абстракциям
- Domain слой не зависит от реализаций
- Инверсия через Hilt DI

## Тестируемость

Архитектура обеспечивает высокую тестируемость:

### Unit тесты
- **Use Cases** - легко тестировать с mock репозиториями
- **ViewModel** - тестирование бизнес-логики UI
- **Repository** - тестирование с mock API и DAO

### Integration тесты
- Тестирование Repository с реальным Room
- Тестирование API с MockWebServer

### UI тесты
- Compose UI Testing
- Тестирование навигации

## Производительность

### Кэширование
- Room для offline-first подхода
- DataStore для быстрого доступа к токенам

### Оптимизация сети
- OkHttp connection pooling
- Gzip compression
- Request retry механизм

### UI
- LazyColumn для списков
- Coil для эффективной загрузки изображений
- State hoisting для переиспользования Composables

## Будущие улучшения

1. **Добавить реальную логику чатов**
   - WebSocket для real-time сообщений
   - Пагинация сообщений
   - Отправка файлов

2. **Улучшить обработку ошибок**
   - Offline mode с синхронизацией
   - Retry стратегии для разных типов ошибок

3. **Расширить тестирование**
   - Unit тесты для всех слоев
   - Screenshot тесты для UI
   - E2E тесты

4. **Оптимизации**
   - Кэширование изображений
   - Prefetch данных
   - Background sync

5. **Security**
   - Certificate pinning
   - Шифрование локального хранилища
   - Biometric authentication