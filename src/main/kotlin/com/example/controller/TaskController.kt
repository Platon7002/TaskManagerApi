package com.example.controller

import com.example.dto.request.CreateTaskRequest
import com.example.dto.response.TaskResponse
import com.example.mapper.toResponse
import com.example.service.TaskService
import com.example.service.UserService
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService,
    private val userService: UserService
) {

    @PostMapping
    suspend fun createTask(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<Any> =
        taskService.create(request).toResponseEntity(HttpStatus.CREATED)

    @GetMapping("/{id}")
    suspend fun getTask(@PathVariable id: Long): ResponseEntity<Any> =
        taskService.getById(id).toResponseEntity()

    @PatchMapping("/{id}/complete")
    suspend fun completeTask(@PathVariable id: Long): ResponseEntity<Any> =
        taskService.complete(id).toResponseEntity()

    @DeleteMapping("/{id}")
    suspend fun deleteTask(@PathVariable id: Long): ResponseEntity<Any> =
        taskService.delete(id).toResponseEntity()

    /** Стрим всех задач (Server-Sent Events): задачи приходят по одной. */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamTasks(): Flow<TaskResponse> =
        taskService.streamAll().map { it.toResponse() }

    /** Стрим задач одного пользователя. */
    @GetMapping("/user/{userId}/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamUserTasks(@PathVariable userId: Long): Flow<TaskResponse> =
        taskService.streamByUser(userId).map { it.toResponse() }

    /** Тот же dashboard, что и /api/users/{id}/dashboard, по адресу из примера curl в задании. */
    @GetMapping("/user/{userId}/dashboard")
    suspend fun getUserDashboard(@PathVariable userId: Long): ResponseEntity<Any> =
        userService.getDashboard(userId).toResponseEntity()
}
