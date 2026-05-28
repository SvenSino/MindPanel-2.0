package com.mindpanel.api.controller

import com.mindpanel.api.model.Todo
import com.mindpanel.api.service.TodoService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
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
    fun createTodo(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody request: CreateTodoRequest): Todo =
        todoService.createTodo(jwt.subject, request.title, request.dueDate, request.isPriority ?: false)

    @PutMapping("/{todoId}")
    fun updateTodo(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable todoId: String,
        @Valid @RequestBody request: UpdateTodoRequest
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

data class CreateTodoRequest(
    @field:NotBlank(message = "Titel darf nicht leer sein")
    @field:Size(max = 255, message = "Titel darf maximal 255 Zeichen lang sein")
    val title: String,
    val dueDate: LocalDate? = null,
    val isPriority: Boolean? = false
)

data class UpdateTodoRequest(
    @field:Size(max = 255, message = "Titel darf maximal 255 Zeichen lang sein")
    val title: String? = null,
    val completed: Boolean? = null,
    val isPriority: Boolean? = null,
    val dueDate: LocalDate? = null
)
