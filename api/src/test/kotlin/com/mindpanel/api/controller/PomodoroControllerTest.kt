package com.mindpanel.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mindpanel.api.model.PomodoroSettings
import com.mindpanel.api.model.PomodoroStat
import com.mindpanel.api.security.SecurityConfig
import com.mindpanel.api.service.PomodoroService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate

@WebMvcTest(PomodoroController::class)
@Import(SecurityConfig::class)
class PomodoroControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @MockitoBean lateinit var pomodoroService: PomodoroService

    private val userId = "user-123"
    private val settings = PomodoroSettings(userId = userId)

    @Test
    fun `GET settings returns user settings`() {
        whenever(pomodoroService.getSettings(userId)).thenReturn(settings)
        mockMvc.get("/api/pomodoro/settings") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
            jsonPath("$.focusDuration") { value(25) }
            jsonPath("$.breakDuration") { value(5) }
        }
    }

    @Test
    fun `GET settings requires authentication`() {
        mockMvc.get("/api/pomodoro/settings")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `PUT settings updates settings`() {
        val updated = settings.copy(focusDuration = 45)
        whenever(pomodoroService.updateSettings(eq(userId), eq(45), eq(10), eq(20), eq(true))).thenReturn(updated)
        val request = PomodoroSettingsRequest(45, 10, 20, true)
        mockMvc.put("/api/pomodoro/settings") {
            with(jwt().jwt { it.subject(userId) })
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.focusDuration") { value(45) }
        }
    }

    @Test
    fun `POST complete records a pomodoro`() {
        val stat = PomodoroStat(userId = userId, date = LocalDate.now(), count = 1)
        whenever(pomodoroService.recordPomodoro(userId)).thenReturn(stat)
        mockMvc.post("/api/pomodoro/complete") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
            jsonPath("$.count") { value(1) }
        }
    }

    @Test
    fun `GET stats returns daily stats map`() {
        val stats = mapOf(LocalDate.now().toString() to 3)
        whenever(pomodoroService.getStats(eq(userId), any(), any())).thenReturn(stats)
        mockMvc.get("/api/pomodoro/stats") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
        }
    }
}
