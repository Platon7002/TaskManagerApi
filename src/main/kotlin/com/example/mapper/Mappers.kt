package com.example.mapper

import com.example.dto.request.CreateTaskRequest
import com.example.dto.request.CreateUserRequest
import com.example.dto.response.TaskResponse
import com.example.dto.response.UserResponse
import com.example.model.Task
import com.example.model.User

// Extension functions: превращают модель в ответ и запрос в модель.
// Лежат отдельно от классов, чтобы модель ничего не знала про DTO.

fun User.toResponse(): UserResponse = UserResponse(id = id, name = name, email = email)

fun Task.toResponse(): TaskResponse = TaskResponse(
    id = id,
    userId = user.id,
    title = title,
    description = description,
    status = status,
    deadline = deadline,
    createdAt = createdAt,
    completedAt = completedAt
)

/** Имя очищается от пробелов, email приводится к нижнему регистру (чтобы Alex@x.ru и alex@x.ru были одним адресом). */
fun CreateUserRequest.toEntity(): User = User(name = name.trim(), email = email.trim().lowercase())

fun CreateTaskRequest.toEntity(owner: User): Task = Task(
    title = title.trim(),
    description = description?.trim()?.takeIf { it.isNotEmpty() },
    deadline = deadline,
    user = owner
)
