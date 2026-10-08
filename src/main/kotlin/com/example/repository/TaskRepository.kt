package com.example.repository

import com.example.model.Task
import com.example.model.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface TaskRepository : JpaRepository<Task, Long> {

    fun findAllByUserId(userId: Long): List<Task>

    fun countByUserId(userId: Long): Long

    fun countByUserIdAndStatus(userId: Long, status: TaskStatus): Long

    fun countByUserIdAndStatusAndDeadlineBefore(userId: Long, status: TaskStatus, date: LocalDate): Long

    @Query(
        "select min(t.deadline) from Task t " +
            "where t.user.id = :userId and t.status = :status and t.deadline >= :today"
    )
    fun findNearestDeadline(
        @Param("userId") userId: Long,
        @Param("status") status: TaskStatus,
        @Param("today") today: LocalDate
    ): LocalDate?
}
