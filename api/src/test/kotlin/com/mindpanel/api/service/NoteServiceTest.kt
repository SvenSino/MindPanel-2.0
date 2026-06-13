package com.mindpanel.api.service

import com.mindpanel.api.model.Note
import com.mindpanel.api.repository.NoteRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.web.server.ResponseStatusException

class NoteServiceTest {

    private val noteRepository: NoteRepository = mock()
    private val noteService = NoteService(noteRepository)

    private val userId = "user-123"
    private val noteId = "note-456"
    private val note = Note(id = noteId, userId = userId, title = "Test", content = "Content")

    @Test
    fun `getNotes returns active notes for user`() {
        whenever(noteRepository.findByUserIdAndArchivedFalse(userId)).thenReturn(listOf(note))
        val result = noteService.getNotes(userId)
        assertEquals(1, result.size)
        assertEquals(note, result[0])
    }

    @Test
    fun `getArchivedNotes returns archived notes for user`() {
        val archived = note.copy(archived = true)
        whenever(noteRepository.findByUserIdAndArchivedTrue(userId)).thenReturn(listOf(archived))
        val result = noteService.getArchivedNotes(userId)
        assertEquals(1, result.size)
        assertTrue(result[0].archived)
    }

    @Test
    fun `createNote saves and returns new note`() {
        whenever(noteRepository.save(any<Note>())).thenAnswer { it.arguments[0] }
        val result = noteService.createNote(userId, "Title", "Body")
        assertEquals("Title", result.title)
        assertEquals("Body", result.content)
        assertEquals(userId, result.userId)
        assertFalse(result.archived)
        verify(noteRepository).save(any())
    }

    @Test
    fun `updateNote changes title and content`() {
        whenever(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(note)
        whenever(noteRepository.save(any<Note>())).thenAnswer { it.arguments[0] }
        val result = noteService.updateNote(userId, noteId, "New Title", "New Content")
        assertEquals("New Title", result.title)
        assertEquals("New Content", result.content)
    }

    @Test
    fun `updateNote throws 404 when note not found`() {
        whenever(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(null)
        assertThrows<ResponseStatusException> {
            noteService.updateNote(userId, noteId, "X", "Y")
        }
    }

    @Test
    fun `archiveNote sets archived to true`() {
        whenever(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(note)
        whenever(noteRepository.save(any<Note>())).thenAnswer { it.arguments[0] }
        val result = noteService.archiveNote(userId, noteId)
        assertTrue(result.archived)
    }

    @Test
    fun `unarchiveNote sets archived to false`() {
        val archived = note.copy(archived = true)
        whenever(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(archived)
        whenever(noteRepository.save(any<Note>())).thenAnswer { it.arguments[0] }
        val result = noteService.unarchiveNote(userId, noteId)
        assertFalse(result.archived)
    }

    @Test
    fun `deleteNote removes note from repository`() {
        whenever(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(note)
        noteService.deleteNote(userId, noteId)
        verify(noteRepository).delete(note)
    }

    @Test
    fun `deleteNote throws 404 when note not found`() {
        whenever(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(null)
        assertThrows<ResponseStatusException> {
            noteService.deleteNote(userId, noteId)
        }
    }
}
