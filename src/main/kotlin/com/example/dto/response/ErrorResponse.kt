package com.example.dto.response

data class ErrorResponse(val error: String)

data class ValidationErrorResponse(
    val message: String,
    val errors: Map<String, String>
)
