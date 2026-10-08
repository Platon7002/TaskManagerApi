package com.example.repository

import com.example.model.Task
import com.example.model.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

/** Запросы пишутся только названиями методов, Spring Data сам создаёт реализацию. */
interface TaskRepository : JpaRepository<Task, Long> {

    fun findAllByUserId(userId: Long): List<Task>

    fun countByUserId(userId: Long): Long

    fun countByUserIdAndStatus(userId: Long, status: TaskStatus): Long

    /** Просроченные: не выполнены и дедлайн раньше указанной даты. */
    fun countByUserIdAndStatusAndDeadlineBefore(userId: Long, status: TaskStatus, date: LocalDate): Long

    /** Ближайший дедлайн среди задач нужного статуса (null, если таких нет). */
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
