package com.example.dto.response

import java.time.LocalDate

/** Сводка по пользователю: сам пользователь + статистика его задач. */
data class DashboardResponse(
    val user: UserResponse,
    val totalTasks: Long,
    val completedTasks: Long,
    val pendingTasks: Long,
    val overdueTasks: Long,
    val completionPercent: Int,
    val nearestDeadline: LocalDate?
)
