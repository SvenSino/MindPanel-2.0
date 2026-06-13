package com.mindpanel.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mindpanel.api.model.DEFAULT_WIDGETS
import com.mindpanel.api.model.WidgetConfig
import com.mindpanel.api.security.SecurityConfig
import com.mindpanel.api.service.WidgetService
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

@WebMvcTest(WidgetController::class)
@Import(SecurityConfig::class)
class WidgetControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @MockitoBean lateinit var widgetService: WidgetService

    private val userId = "user-123"
    private val config = WidgetConfig(userId = userId, widgets = DEFAULT_WIDGETS)

    @Test
    fun `GET widgets returns config`() {
        whenever(widgetService.getWidgets(userId)).thenReturn(config)
        mockMvc.get("/api/widgets") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
            jsonPath("$.widgets.length()") { value(5) }
        }
    }

    @Test
    fun `GET widgets requires authentication`() {
        mockMvc.get("/api/widgets")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `PUT widgets updates config`() {
        whenever(widgetService.updateWidgets(eq(userId), any())).thenReturn(config)
        val request = WidgetUpdateRequest(DEFAULT_WIDGETS)
        mockMvc.put("/api/widgets") {
            with(jwt().jwt { it.subject(userId) })
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `POST reset restores defaults`() {
        whenever(widgetService.resetWidgets(userId)).thenReturn(config)
        mockMvc.post("/api/widgets/reset") {
            with(jwt().jwt { it.subject(userId) })
        }.andExpect {
            status { isOk() }
            jsonPath("$.widgets.length()") { value(5) }
        }
    }
}
