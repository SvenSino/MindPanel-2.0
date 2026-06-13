package com.mindpanel.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mindpanel.api.model.Note
import com.mindpanel.api.security.SecurityConfig
import com.mindpanel.api.service.NoteService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@WebMvcTest(NoteController::class)
@Import(SecurityConfig::class)
class NoteControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @MockitoBean lateinit var noteService: NoteService

    private val userId = "user-123"
    private val noteId = "note-456"
    private val note = Note(id = noteId, userId = userId, title = "Test", content = "Content")

    @Test
    fun `GET notes returns list`() {
        whenever(noteService.getNotes(userId)).thenReturn(listOf(note))
        mockMvc.get("/api/notes") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].title") { value("Test") }
        }
    }

    @Test
    fun `GET notes requires authentication`() {
        mockMvc.get("/api/notes")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `GET archived notes returns archived list`() {
        val archived = note.copy(archived = true)
        whenever(noteService.getArchivedNotes(userId)).thenReturn(listOf(archived))
        mockMvc.get("/api/notes/archived") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].archived") { value(true) }
        }
    }

    @Test
    fun `POST note creates note`() {
        whenever(noteService.createNote(eq(userId), eq("Title"), eq("Body"))).thenReturn(note)
        val request = NoteRequest("Title", "Body")
        mockMvc.post("/api/notes") {
            with(jwt().jwt { it.subject(userId) })
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Test") }
        }
    }

    @Test
    fun `PUT note updates note`() {
        whenever(noteService.updateNote(eq(userId), eq(noteId), eq("Updated"), eq("New body"))).thenReturn(note)
        val request = NoteRequest("Updated", "New body")
        mockMvc.put("/api/notes/$noteId") {
            with(jwt().jwt { it.subject(userId) })
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `POST archive archives note`() {
        whenever(noteService.archiveNote(userId, noteId)).thenReturn(note.copy(archived = true))
        mockMvc.post("/api/notes/$noteId/archive") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
            jsonPath("$.archived") { value(true) }
        }
    }

    @Test
    fun `DELETE note returns 204`() {
        doNothing().whenever(noteService).deleteNote(userId, noteId)
        mockMvc.delete("/api/notes/$noteId") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isNoContent() }
        }
    }
}
