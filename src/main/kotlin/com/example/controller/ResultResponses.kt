package com.example.controller

import com.example.dto.response.ErrorResponse
import com.example.mapper.toResponse
import com.example.service.result.DashboardResult
import com.example.service.result.TaskResult
import com.example.service.result.UserResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

// Extension functions превращают результат сервиса (sealed class) в HTTP-ответ.
// Они лежат отдельно от контроллеров. Во всех when нет else: компилятор сам
// проверяет, что обработаны ВСЕ варианты результата.

fun errorResponse(status: HttpStatus, message: String): ResponseEntity<Any> =
    ResponseEntity.status(status).body(ErrorResponse(message))

fun UserResult.toResponseEntity(successStatus: HttpStatus = HttpStatus.OK): ResponseEntity<Any> = when (this) {
    is UserResult.Success -> ResponseEntity.status(successStatus).body(user.toResponse())
    is UserResult.NotFound -> errorResponse(HttpStatus.NOT_FOUND, "Пользователь с ID $id не найден")
    is UserResult.EmailAlreadyUsed -> errorResponse(HttpStatus.CONFLICT, "Пользователь с email $email уже существует")
}

fun DashboardResult.toResponseEntity(): ResponseEntity<Any> = when (this) {
    is DashboardResult.Success -> ResponseEntity.ok(dashboard)
    is DashboardResult.NotFound -> errorResponse(HttpStatus.NOT_FOUND, "Пользователь с ID $userId не найден")
}

fun TaskResult.toResponseEntity(successStatus: HttpStatus = HttpStatus.OK): ResponseEntity<Any> = when (this) {
    is TaskResult.Success -> ResponseEntity.status(successStatus).body(task.toResponse())
    is TaskResult.Deleted -> ResponseEntity.noContent().build()
    is TaskResult.NotFound -> errorResponse(HttpStatus.NOT_FOUND, "Задача с ID $id не найдена")
    is TaskResult.UserNotFound -> errorResponse(HttpStatus.NOT_FOUND, "Пользователь с ID $userId не найден")
    is TaskResult.AlreadyCompleted -> errorResponse(HttpStatus.CONFLICT, "Задача с ID $id уже выполнена")
}
