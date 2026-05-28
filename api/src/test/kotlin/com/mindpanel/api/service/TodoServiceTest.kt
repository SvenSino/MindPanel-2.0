package com.mindpanel.api.service

import com.mindpanel.api.model.Todo
import com.mindpanel.api.repository.TodoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

class TodoServiceTest {

    private val todoRepository: TodoRepository = mock()
    private val todoService = TodoService(todoRepository)

    private val userId = "user-123"
    private val todoId = "todo-456"
    private val todo = Todo(id = todoId, userId = userId, title = "Task")

    @Test
    fun `getTodos returns active todos`() {
        whenever(todoRepository.findByUserIdAndArchivedFalse(userId)).thenReturn(listOf(todo))
        val result = todoService.getTodos(userId)
        assertEquals(1, result.size)
    }

    @Test
    fun `getArchivedTodos returns archived todos`() {
        val archived = todo.copy(archived = true)
        whenever(todoRepository.findByUserIdAndArchivedTrue(userId)).thenReturn(listOf(archived))
        val result = todoService.getArchivedTodos(userId)
        assertTrue(result[0].archived)
    }

    @Test
    fun `createTodo saves new todo`() {
        whenever(todoRepository.countByUserIdAndIsPriorityTrueAndArchivedFalse(userId)).thenReturn(0)
        whenever(todoRepository.save(any<Todo>())).thenAnswer { it.arguments[0] }
        val result = todoService.createTodo(userId, "Buy milk", null, false)
        assertEquals("Buy milk", result.title)
        assertEquals(userId, result.userId)
    }

    @Test
    fun `createTodo with priority checks limit`() {
        whenever(todoRepository.countByUserIdAndIsPriorityTrueAndArchivedFalse(userId)).thenReturn(0)
        whenever(todoRepository.save(any<Todo>())).thenAnswer { it.arguments[0] }
        val result = todoService.createTodo(userId, "Urgent task", null, true)
        assertTrue(result.isPriority)
    }

    @Test
    fun `createTodo throws 400 when priority limit reached`() {
        whenever(todoRepository.countByUserIdAndIsPriorityTrueAndArchivedFalse(userId)).thenReturn(3)
        val ex = assertThrows<ResponseStatusException> {
            todoService.createTodo(userId, "Another priority", null, true)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `updateTodo changes title and completion`() {
        whenever(todoRepository.findByIdAndUserId(todoId, userId)).thenReturn(todo)
        whenever(todoRepository.save(any<Todo>())).thenAnswer { it.arguments[0] }
        val result = todoService.updateTodo(userId, todoId, "Updated", true, null, null)
        assertEquals("Updated", result.title)
        assertTrue(result.completed)
    }

    @Test
    fun `updateTodo with dueDate updates correctly`() {
        val dueDate = LocalDate.of(2026, 5, 1)
        whenever(todoRepository.findByIdAndUserId(todoId, userId)).thenReturn(todo)
        whenever(todoRepository.save(any<Todo>())).thenAnswer { it.arguments[0] }
        val result = todoService.updateTodo(userId, todoId, null, null, null, dueDate)
        assertEquals(dueDate, result.dueDate)
    }

    @Test
    fun `updateTodo throws 404 when not found`() {
        whenever(todoRepository.findByIdAndUserId(todoId, userId)).thenReturn(null)
        assertThrows<ResponseStatusException> {
            todoService.updateTodo(userId, todoId, "x", null, null, null)
        }
    }

    @Test
    fun `archiveTodo sets archived to true`() {
        whenever(todoRepository.findByIdAndUserId(todoId, userId)).thenReturn(todo)
        whenever(todoRepository.save(any<Todo>())).thenAnswer { it.arguments[0] }
        val result = todoService.archiveTodo(userId, todoId)
        assertTrue(result.archived)
    }

    @Test
    fun `unarchiveTodo sets archived to false`() {
        val archived = todo.copy(archived = true)
        whenever(todoRepository.findByIdAndUserId(todoId, userId)).thenReturn(archived)
        whenever(todoRepository.save(any<Todo>())).thenAnswer { it.arguments[0] }
        val result = todoService.unarchiveTodo(userId, todoId)
        assertFalse(result.archived)
    }

    @Test
    fun `deleteTodo removes todo`() {
        whenever(todoRepository.findByIdAndUserId(todoId, userId)).thenReturn(todo)
        todoService.deleteTodo(userId, todoId)
        verify(todoRepository).delete(todo)
    }
}
