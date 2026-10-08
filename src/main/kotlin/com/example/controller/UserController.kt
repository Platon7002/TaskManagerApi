package com.example.controller

import com.example.dto.request.CreateUserRequest
import com.example.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Контроллер только принимает запрос и вызывает сервис. Правил и логики здесь нет. */
@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping
    suspend fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<Any> =
        userService.create(request).toResponseEntity(HttpStatus.CREATED)

    @GetMapping("/{id}")
    suspend fun getUser(@PathVariable id: Long): ResponseEntity<Any> =
        userService.getById(id).toResponseEntity()

    @GetMapping("/{id}/dashboard")
    suspend fun getDashboard(@PathVariable id: Long): ResponseEntity<Any> =
        userService.getDashboard(id).toResponseEntity()
}
