package com.mindpanel.api.service

import com.mindpanel.api.model.Note
import com.mindpanel.api.repository.NoteRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class NoteService(private val noteRepository: NoteRepository) {

    fun getNotes(userId: String): List<Note> =
        noteRepository.findByUserIdAndArchivedFalse(userId)

    fun getArchivedNotes(userId: String): List<Note> =
        noteRepository.findByUserIdAndArchivedTrue(userId)

    fun createNote(userId: String, title: String, content: String): Note {
        val note = Note(userId = userId, title = title, content = content)
        return noteRepository.save(note)
    }

    fun updateNote(userId: String, noteId: String, title: String, content: String): Note {
        val note = findOwnedNote(userId, noteId)
        return noteRepository.save(note.copy(title = title, content = content))
    }

    fun archiveNote(userId: String, noteId: String): Note {
        val note = findOwnedNote(userId, noteId)
        return noteRepository.save(note.copy(archived = true))
    }

    fun unarchiveNote(userId: String, noteId: String): Note {
        val note = findOwnedNote(userId, noteId)
        return noteRepository.save(note.copy(archived = false))
    }

    fun deleteNote(userId: String, noteId: String) {
        val note = findOwnedNote(userId, noteId)
        noteRepository.delete(note)
    }

    private fun findOwnedNote(userId: String, noteId: String): Note =
        noteRepository.findByIdAndUserId(noteId, userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
}
