package com.mindpanel.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mindpanel.api.model.Todo
import com.mindpanel.api.security.SecurityConfig
import com.mindpanel.api.service.TodoService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.web.server.ResponseStatusException

@WebMvcTest(TodoController::class)
@Import(SecurityConfig::class)
class TodoControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @MockitoBean lateinit var todoService: TodoService

    private val userId = "user-123"
    private val todoId = "todo-456"
    private val todo = Todo(id = todoId, userId = userId, title = "Task")

    @Test
    fun `GET todos returns list`() {
        whenever(todoService.getTodos(userId)).thenReturn(listOf(todo))
        mockMvc.get("/api/todos") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].title") { value("Task") }
        }
    }

    @Test
    fun `GET todos requires authentication`() {
        mockMvc.get("/api/todos")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `GET archived todos returns archived list`() {
        whenever(todoService.getArchivedTodos(userId)).thenReturn(listOf(todo.copy(archived = true)))
        mockMvc.get("/api/todos/archived") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].archived") { value(true) }
        }
    }

    @Test
    fun `POST todo creates todo`() {
        whenever(todoService.createTodo(eq(userId), eq("Task"), isNull(), eq(false))).thenReturn(todo)
        val request = CreateTodoRequest("Task")
        mockMvc.post("/api/todos") {
            with(jwt().jwt { it.subject(userId) })
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Task") }
        }
    }

    @Test
    fun `POST todo returns 400 when priority limit exceeded`() {
        whenever(todoService.createTodo(any(), any(), anyOrNull(), any()))
            .thenThrow(ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum of 3 priority todos allowed"))
        val request = CreateTodoRequest("Priority Task", isPriority = true)
        mockMvc.post("/api/todos") {
            with(jwt().jwt { it.subject(userId) })
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `PUT todo updates todo`() {
        whenever(todoService.updateTodo(eq(userId), eq(todoId), any(), any(), any(), anyOrNull())).thenReturn(todo)
        val request = UpdateTodoRequest(completed = true)
        mockMvc.put("/api/todos/$todoId") {
            with(jwt().jwt { it.subject(userId) })
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `POST archive archives todo`() {
        whenever(todoService.archiveTodo(userId, todoId)).thenReturn(todo.copy(archived = true))
        mockMvc.post("/api/todos/$todoId/archive") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
            jsonPath("$.archived") { value(true) }
        }
    }

    @Test
    fun `DELETE todo returns 204`() {
        doNothing().whenever(todoService).deleteTodo(userId, todoId)
        mockMvc.delete("/api/todos/$todoId") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isNoContent() }
        }
    }
}
