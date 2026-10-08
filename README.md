## Проект: Task Manager API

REST API на Kotlin + Spring Boot для управления пользователями и их задачами.
Сущности: `User` 1 — N `Task`. База данных H2 (в памяти, при каждом запуске чистая).

### Запуск
```bash
./gradlew bootRun          # Linux / macOS
gradlew.bat bootRun        # Windows
```
Нужен JDK 21. Сервер стартует на http://localhost:8080

### Эндпоинты
| Метод | Путь | Описание |
|---|---|---|
| POST | `/api/users` | создать пользователя (`name`, `email`) |
| GET | `/api/users/{id}` | получить пользователя |
| GET | `/api/users/{id}/dashboard` | сводка: пользователь + статистика (параллельные запросы, `async`) |
| POST | `/api/tasks` | создать задачу (`userId`, `title`, `description?`, `deadline?`) |
| GET | `/api/tasks/{id}` | получить задачу |
| PATCH | `/api/tasks/{id}/complete` | отметить выполненной |
| DELETE | `/api/tasks/{id}` | удалить задачу |
| GET | `/api/tasks/stream` | стрим всех задач (Flow, SSE) |
| GET | `/api/tasks/user/{userId}/stream` | стрим задач пользователя (Flow, SSE) |
| GET | `/api/tasks/user/{userId}/dashboard` | то же, что `/api/users/{id}/dashboard` |

### Что использовано из Kotlin
- `data class` для всех DTO: `CreateUserRequest`, `CreateTaskRequest`, `UserResponse`, `TaskResponse`, `DashboardResponse`, `ErrorResponse`
- `sealed class`: `UserResult`, `DashboardResult`, `TaskResult`; `when` по ним без `else`
- Extension functions: `User.toResponse()`, `Task.toResponse()`, `CreateUserRequest.toEntity()`, `CreateTaskRequest.toEntity()`, `UserResult.toResponseEntity()`, `TaskResult.toResponseEntity()`, `DashboardResult.toResponseEntity()`
- Null-safety: оператор `!!` нигде не используется (`?.`, `?:`, `takeIf`, `findByIdOrNull`)
- Корутины: все эндпоинты `suspend`; `async { }.await()` в `getDashboard()`; `Flow` в `streamTasks()`; блокирующие вызовы JPA выполняются в `withContext(Dispatchers.IO)`; вместо `Thread.sleep` используется `delay`

### Валидация и ошибки
Аннотации `@field:NotBlank`, `@field:Positive`, `@field:Email`, `@field:Size` на DTO; единый `@RestControllerAdvice`.
Форматы ошибок: `{"error": "..."}` и `{"message": "...", "errors": {"поле": "сообщение"}}`.

### Частые ошибки
```kotlin
// Плохо: блокирующий вызов внутри корутины без withContext
suspend fun getTask(id: Long): Task = taskRepository.findById(id).orElseThrow()

// Хорошо
suspend fun getTask(id: Long): Task? = withContext(Dispatchers.IO) { taskRepository.findByIdOrNull(id) }

// Плохо: async без coroutineScope (не скомпилируется)
suspend fun getDashboard(userId: Long) { val total = async { taskRepository.countByUserId(userId) } }

// Хорошо
suspend fun getDashboard(userId: Long) = coroutineScope { val total = async(Dispatchers.IO) { taskRepository.countByUserId(userId) } }

// Плохо: аннотация валидации без @field: (в Kotlin не сработает)
data class CreateTaskRequest(@NotBlank val title: String)

// Хорошо
data class CreateTaskRequest(@field:NotBlank(message = "Название обязательно") val title: String)
```
