package com.mindpanel.api.repository

import com.mindpanel.api.model.Note
import org.springframework.data.mongodb.repository.MongoRepository

interface NoteRepository : MongoRepository<Note, String> {
    fun findByUserIdAndArchivedFalse(userId: String): List<Note>
    fun findByUserIdAndArchivedTrue(userId: String): List<Note>
    fun findByIdAndUserId(id: String, userId: String): Note?
}
