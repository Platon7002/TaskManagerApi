package com.example.service.result

import com.example.model.Task

/** Все возможные итоги операций с задачей. when по этому классу не требует else. */
sealed class TaskResult {
    data class Success(val task: Task) : TaskResult()
    data class Deleted(val id: Long) : TaskResult()
    data class NotFound(val id: Long) : TaskResult()
    data class UserNotFound(val userId: Long) : TaskResult()
    data class AlreadyCompleted(val id: Long) : TaskResult()
}
