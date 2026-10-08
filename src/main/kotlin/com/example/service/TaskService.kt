package com.example.service

import com.example.dto.request.CreateTaskRequest
import com.example.mapper.toEntity
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.repository.TaskRepository
import com.example.repository.UserRepository
import com.example.service.result.TaskResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/** Пауза между элементами потока, чтобы стрим было видно «живым» (delay не блокирует поток). */
private const val STREAM_DELAY_MS = 300L

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {

    suspend fun create(request: CreateTaskRequest): TaskResult = withContext(Dispatchers.IO) {
        val owner = userRepository.findByIdOrNull(request.userId)
            ?: return@withContext TaskResult.UserNotFound(request.userId)
        TaskResult.Success(taskRepository.save(request.toEntity(owner)))
    }

    suspend fun getById(id: Long): TaskResult = withContext(Dispatchers.IO) {
        val task = taskRepository.findByIdOrNull(id)
        if (task == null) TaskResult.NotFound(id) else TaskResult.Success(task)
    }

    /** Отмечает задачу выполненной. Повторное завершение даёт AlreadyCompleted. */
    suspend fun complete(id: Long): TaskResult = withContext(Dispatchers.IO) {
        val task = taskRepository.findByIdOrNull(id)
            ?: return@withContext TaskResult.NotFound(id)
        if (task.status == TaskStatus.COMPLETED) {
            return@withContext TaskResult.AlreadyCompleted(id)
        }
        task.status = TaskStatus.COMPLETED
        task.completedAt = LocalDateTime.now().withNano(0)
        TaskResult.Success(taskRepository.save(task))
    }

    suspend fun delete(id: Long): TaskResult = withContext(Dispatchers.IO) {
        if (!taskRepository.existsById(id)) {
            return@withContext TaskResult.NotFound(id)
        }
        taskRepository.deleteById(id)
        TaskResult.Deleted(id)
    }

    /** Поток всех задач: элементы отдаются по одному, а не списком целиком. */
    fun streamAll(): Flow<Task> = flow {
        val tasks = withContext(Dispatchers.IO) { taskRepository.findAll() }
        for (task in tasks) {
            emit(task)
            delay(STREAM_DELAY_MS)
        }
    }

    /** Поток задач одного пользователя. Если пользователя нет, поток просто пустой. */
    fun streamByUser(userId: Long): Flow<Task> = flow {
        val tasks = withContext(Dispatchers.IO) { taskRepository.findAllByUserId(userId) }
        for (task in tasks) {
            emit(task)
            delay(STREAM_DELAY_MS)
        }
    }
}
