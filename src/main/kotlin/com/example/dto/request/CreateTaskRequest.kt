package com.example.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class CreateTaskRequest(
    @field:Positive(message = "userId должен быть положительным числом")
    val userId: Long = 0,

    @field:NotBlank(message = "Название задачи обязательно")
    @field:Size(max = 200, message = "Название не должно быть длиннее 200 символов")
    val title: String = "",

    @field:Size(max = 1000, message = "Описание не должно быть длиннее 1000 символов")
    val description: String? = null,

    val deadline: LocalDate? = null
)
