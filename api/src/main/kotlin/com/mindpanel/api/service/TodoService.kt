package com.mindpanel.api.service

import com.mindpanel.api.model.Todo
import com.mindpanel.api.repository.TodoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
class TodoService(private val todoRepository: TodoRepository) {

    fun getTodos(userId: String): List<Todo> =
        todoRepository.findByUserIdAndArchivedFalse(userId)

    fun getArchivedTodos(userId: String): List<Todo> =
        todoRepository.findByUserIdAndArchivedTrue(userId)

    fun createTodo(userId: String, title: String, dueDate: LocalDate?, isPriority: Boolean): Todo {
        if (isPriority) checkPriorityLimit(userId)
        val todo = Todo(userId = userId, title = title, dueDate = dueDate, isPriority = isPriority)
        return todoRepository.save(todo)
    }

    fun updateTodo(userId: String, todoId: String, title: String?, completed: Boolean?, isPriority: Boolean?, dueDate: LocalDate?): Todo {
        val todo = findOwnedTodo(userId, todoId)
        val newIsPriority = isPriority ?: todo.isPriority
        if (newIsPriority && !todo.isPriority) checkPriorityLimit(userId)
        return todoRepository.save(
            todo.copy(
                title = title ?: todo.title,
                completed = completed ?: todo.completed,
                isPriority = newIsPriority,
                dueDate = dueDate ?: todo.dueDate
            )
        )
    }

    fun archiveTodo(userId: String, todoId: String): Todo {
        val todo = findOwnedTodo(userId, todoId)
        return todoRepository.save(todo.copy(archived = true))
    }

    fun unarchiveTodo(userId: String, todoId: String): Todo {
        val todo = findOwnedTodo(userId, todoId)
        return todoRepository.save(todo.copy(archived = false))
    }

    fun deleteTodo(userId: String, todoId: String) {
        val todo = findOwnedTodo(userId, todoId)
        todoRepository.delete(todo)
    }

    private fun checkPriorityLimit(userId: String) {
        if (todoRepository.countByUserIdAndIsPriorityTrueAndArchivedFalse(userId) >= 3) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum of 3 priority todos allowed")
        }
    }

    private fun findOwnedTodo(userId: String, todoId: String): Todo =
        todoRepository.findByIdAndUserId(todoId, userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found")
}
