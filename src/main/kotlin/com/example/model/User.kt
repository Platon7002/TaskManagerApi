package com.example.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

/**
 * Пользователь. Одна сторона связи User 1 — N Task.
 * Это JPA-сущность, поэтому обычный class (а не data class): у сущностей своя идентичность.
 */
@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, unique = true, length = 150)
    var email: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    // Список задач загружается лениво и снаружи не используется:
    // статистику считают запросы count в TaskRepository.
    @OneToMany(mappedBy = "user")
    val tasks: MutableList<Task> = mutableListOf()
}
