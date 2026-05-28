package com.mindpanel.api.controller

import com.mindpanel.api.model.Note
import com.mindpanel.api.service.NoteService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notes")
class NoteController(private val noteService: NoteService) {

    @GetMapping
    fun getNotes(@AuthenticationPrincipal jwt: Jwt): List<Note> =
        noteService.getNotes(jwt.subject)

    @GetMapping("/archived")
    fun getArchivedNotes(@AuthenticationPrincipal jwt: Jwt): List<Note> =
        noteService.getArchivedNotes(jwt.subject)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createNote(@AuthenticationPrincipal jwt: Jwt, @RequestBody request: NoteRequest): Note =
        noteService.createNote(jwt.subject, request.title, request.content)

    @PutMapping("/{noteId}")
    fun updateNote(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable noteId: String,
        @RequestBody request: NoteRequest
    ): Note = noteService.updateNote(jwt.subject, noteId, request.title, request.content)

    @PostMapping("/{noteId}/archive")
    fun archiveNote(@AuthenticationPrincipal jwt: Jwt, @PathVariable noteId: String): Note =
        noteService.archiveNote(jwt.subject, noteId)

    @PostMapping("/{noteId}/unarchive")
    fun unarchiveNote(@AuthenticationPrincipal jwt: Jwt, @PathVariable noteId: String): Note =
        noteService.unarchiveNote(jwt.subject, noteId)

    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteNote(@AuthenticationPrincipal jwt: Jwt, @PathVariable noteId: String) =
        noteService.deleteNote(jwt.subject, noteId)
}

data class NoteRequest(val title: String, val content: String)
