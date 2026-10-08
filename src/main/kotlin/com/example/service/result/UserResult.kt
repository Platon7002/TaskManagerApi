package com.example.service.result

import com.example.dto.response.DashboardResponse
import com.example.model.User

/** Все возможные итоги операций с пользователем. when по этому классу не требует else. */
sealed class UserResult {
    data class Success(val user: User) : UserResult()
    data class NotFound(val id: Long) : UserResult()
    data class EmailAlreadyUsed(val email: String) : UserResult()
}

/** Итоги получения сводки (dashboard). */
sealed class DashboardResult {
    data class Success(val dashboard: DashboardResponse) : DashboardResult()
    data class NotFound(val userId: Long) : DashboardResult()
}
