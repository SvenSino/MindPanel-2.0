package com.mindpanel.api.controller

import com.mindpanel.api.model.Todo
import com.mindpanel.api.service.TodoService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoService) {

    @GetMapping
    fun getTodos(@AuthenticationPrincipal jwt: Jwt): List<Todo> =
        todoService.getTodos(jwt.subject)

    @GetMapping("/archived")
    fun getArchivedTodos(@AuthenticationPrincipal jwt: Jwt): List<Todo> =
        todoService.getArchivedTodos(jwt.subject)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTodo(@AuthenticationPrincipal jwt: Jwt, @RequestBody request: CreateTodoRequest): Todo =
        todoService.createTodo(jwt.subject, request.title, request.dueDate, request.isPriority ?: false)

    @PutMapping("/{todoId}")
    fun updateTodo(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable todoId: String,
        @RequestBody request: UpdateTodoRequest
    ): Todo = todoService.updateTodo(jwt.subject, todoId, request.title, request.completed, request.isPriority, request.dueDate)

    @PostMapping("/{todoId}/archive")
    fun archiveTodo(@AuthenticationPrincipal jwt: Jwt, @PathVariable todoId: String): Todo =
        todoService.archiveTodo(jwt.subject, todoId)

    @PostMapping("/{todoId}/unarchive")
    fun unarchiveTodo(@AuthenticationPrincipal jwt: Jwt, @PathVariable todoId: String): Todo =
        todoService.unarchiveTodo(jwt.subject, todoId)

    @DeleteMapping("/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTodo(@AuthenticationPrincipal jwt: Jwt, @PathVariable todoId: String) =
        todoService.deleteTodo(jwt.subject, todoId)
}

data class CreateTodoRequest(val title: String, val dueDate: LocalDate? = null, val isPriority: Boolean? = false)
data class UpdateTodoRequest(val title: String? = null, val completed: Boolean? = null, val isPriority: Boolean? = null, val dueDate: LocalDate? = null)
