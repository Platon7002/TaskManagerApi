package com.example.service

import com.example.dto.request.CreateUserRequest
import com.example.dto.response.DashboardResponse
import com.example.mapper.toEntity
import com.example.mapper.toResponse
import com.example.model.TaskStatus
import com.example.repository.TaskRepository
import com.example.repository.UserRepository
import com.example.service.result.DashboardResult
import com.example.service.result.UserResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UserService(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) {

    // JPA-репозитории блокирующие, поэтому каждый вызов уходит на Dispatchers.IO:
    // так обработка запросов не занимает основной поток.

    suspend fun create(request: CreateUserRequest): UserResult = withContext(Dispatchers.IO) {
        val user = request.toEntity()
        if (userRepository.existsByEmail(user.email)) {
            return@withContext UserResult.EmailAlreadyUsed(user.email)
        }
        try {
            UserResult.Success(userRepository.save(user))
        } catch (e: DataIntegrityViolationException) {
            // Два одинаковых запроса пришли одновременно, и уникальный индекс в базе сработал
            UserResult.EmailAlreadyUsed(user.email)
        }
    }

    suspend fun getById(id: Long): UserResult = withContext(Dispatchers.IO) {
        val user = userRepository.findByIdOrNull(id)
        if (user == null) UserResult.NotFound(id) else UserResult.Success(user)
    }

    /**
     * Сводка по пользователю. Пять независимых запросов к базе выполняются ПАРАЛЛЕЛЬНО
     * (async), а затем результаты собираются через await().
     * coroutineScope гарантирует, что функция не завершится, пока не закончатся все async.
     */
    suspend fun getDashboard(userId: Long): DashboardResult = coroutineScope {
        val today = LocalDate.now()

        val userDeferred = async(Dispatchers.IO) { userRepository.findByIdOrNull(userId) }
        val totalDeferred = async(Dispatchers.IO) { taskRepository.countByUserId(userId) }
        val completedDeferred = async(Dispatchers.IO) {
            taskRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED)
        }
        val overdueDeferred = async(Dispatchers.IO) {
            taskRepository.countByUserIdAndStatusAndDeadlineBefore(userId, TaskStatus.PENDING, today)
        }
        val deadlineDeferred = async(Dispatchers.IO) {
            taskRepository.findNearestDeadline(userId, TaskStatus.PENDING, today)
        }

        val user = userDeferred.await()
            ?: return@coroutineScope DashboardResult.NotFound(userId)
        val total = totalDeferred.await()
        val completed = completedDeferred.await()

        DashboardResult.Success(
            DashboardResponse(
                user = user.toResponse(),
                totalTasks = total,
                completedTasks = completed,
                pendingTasks = total - completed,
                overdueTasks = overdueDeferred.await(),
                completionPercent = if (total == 0L) 0 else (completed * 100 / total).toInt(),
                nearestDeadline = deadlineDeferred.await()
            )
        )
    }
}
