@echo off
chcp 65001 >nul
echo ===== Task Manager API: демонстрация (сервер должен быть запущен на порту 8080) =====

echo.
echo --- 1. Создать пользователя ---
curl -s -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d "{\"name\":\"Alex\",\"email\":\"alex@mail.ru\"}"

echo.
echo --- 2. Создать две задачи ---
curl -s -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -d "{\"userId\":1,\"title\":\"Learn coroutines\",\"deadline\":\"2024-12-31\"}"
echo.
curl -s -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -d "{\"userId\":1,\"title\":\"Buy bread\"}"

echo.
echo --- 3. Отметить первую задачу выполненной ---
curl -s -X PATCH http://localhost:8080/api/tasks/1/complete

echo.
echo --- 4. Dashboard (параллельные запросы) ---
curl -s http://localhost:8080/api/tasks/user/1/dashboard

echo.
echo --- 5. Flow-стрим задач (SSE, задачи приходят по одной) ---
curl -s -N --max-time 10 http://localhost:8080/api/tasks/user/1/stream

echo.
echo --- 6. Ошибка валидации: должен вернуться 400 ---
curl -s -i -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -d "{\"userId\":-1,\"title\":\"\"}"

echo.
echo --- 7. Несуществующая задача: 404 ---
curl -s http://localhost:8080/api/tasks/999

echo.
echo --- 8. Удалить задачу 2 ---
curl -s -i -X DELETE http://localhost:8080/api/tasks/2

echo.
echo ===== Готово. Повторный запуск без перезапуска сервера даст 409 (такой email уже есть) =====
pause
