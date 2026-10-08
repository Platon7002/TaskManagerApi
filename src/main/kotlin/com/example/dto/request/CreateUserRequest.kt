package com.example.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Запрос на создание пользователя.
 * Аннотации с @field: обязательны: без них валидация в Kotlin не срабатывает.
 * Значения по умолчанию (= "") нужны, чтобы при отсутствии поля в JSON
 * пользователь получил понятное сообщение валидации, а не ошибку разбора JSON.
 */
data class CreateUserRequest(
    @field:NotBlank(message = "Имя обязательно")
    @field:Size(max = 100, message = "Имя не должно быть длиннее 100 символов")
    val name: String = "",

    @field:NotBlank(message = "Email обязателен")
    @field:Email(message = "Некорректный email")
    @field:Size(max = 150, message = "Email не должен быть длиннее 150 символов")
    val email: String = ""
)
