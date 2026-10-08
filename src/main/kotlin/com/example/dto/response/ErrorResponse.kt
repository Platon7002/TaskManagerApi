package com.example.dto.response

/** Единый формат простой ошибки: {"error": "..."} */
data class ErrorResponse(val error: String)

/** Единый формат ошибки валидации: {"message": "...", "errors": {"поле": "сообщение"}} */
data class ValidationErrorResponse(
    val message: String,
    val errors: Map<String, String>
)
