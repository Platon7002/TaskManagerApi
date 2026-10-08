package com.example.exception

import com.example.dto.response.ErrorResponse
import com.example.dto.response.ValidationErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.io.IOException

/**
 * Единый обработчик ошибок. Любая ошибка превращается в JSON одного из двух форматов:
 *   {"error": "..."}
 *   {"message": "...", "errors": {"поле": "сообщение"}}
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /** Ошибки валидации (@field:NotBlank и другие): 400 со списком полей. */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .sortedBy { it.field }
            .associate { it.field to (it.defaultMessage ?: "Некорректное значение") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse("Ошибка валидации входных данных", errors))
    }

    /** Битый JSON, неверный тип поля или неверный формат даты. */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(): ResponseEntity<ErrorResponse> =
        badRequest("Некорректное тело запроса: ожидается JSON, даты в формате ГГГГ-ММ-ДД (например, 2024-12-31)")

    /** Например, /api/users/abc вместо числа. */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> =
        badRequest("Параметр '${ex.name}' имеет неверный формат: ожидается число")

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaType(): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ErrorResponse("Используйте заголовок Content-Type: application/json"))

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ErrorResponse("Метод ${ex.method} для этого адреса не поддерживается"))

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNotFound(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("Адрес /${ex.resourcePath} не найден"))

    /** Клиент закрыл соединение (например, остановил curl -N во время стрима): это не ошибка сервера. */
    @ExceptionHandler(IOException::class)
    fun handleClientDisconnect(ex: IOException) {
        log.debug("Соединение с клиентом закрыто: {}", ex.message)
    }

    /** Всё остальное: 500. Подробности пишутся в лог, клиенту они не показываются. */
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Необработанная ошибка", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("Внутренняя ошибка сервера"))
    }

    private fun badRequest(message: String): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(message))
}
