package com.mindpanel.api.repository

import com.mindpanel.api.model.Todo
import org.springframework.data.mongodb.repository.MongoRepository

interface TodoRepository : MongoRepository<Todo, String> {
    fun findByUserIdAndArchivedFalse(userId: String): List<Todo>
    fun findByUserIdAndArchivedTrue(userId: String): List<Todo>
    fun findByIdAndUserId(id: String, userId: String): Todo?
    fun countByUserIdAndIsPriorityTrueAndArchivedFalse(userId: String): Int
}
