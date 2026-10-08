package com.example.service.result

import com.example.dto.response.DashboardResponse
import com.example.model.User

sealed class UserResult {
    data class Success(val user: User) : UserResult()
    data class NotFound(val id: Long) : UserResult()
    data class EmailAlreadyUsed(val email: String) : UserResult()
}

sealed class DashboardResult {
    data class Success(val dashboard: DashboardResponse) : DashboardResult()
    data class NotFound(val userId: Long) : DashboardResult()
}
