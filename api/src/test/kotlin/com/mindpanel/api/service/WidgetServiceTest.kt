package com.mindpanel.api.service

import com.mindpanel.api.model.DEFAULT_WIDGETS
import com.mindpanel.api.model.Widget
import com.mindpanel.api.model.WidgetConfig
import com.mindpanel.api.repository.WidgetConfigRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class WidgetServiceTest {

    private val widgetConfigRepository: WidgetConfigRepository = mock()
    private val widgetService = WidgetService(widgetConfigRepository)

    private val userId = "user-123"
    private val existingConfig = WidgetConfig(userId = userId, widgets = DEFAULT_WIDGETS)

    @Test
    fun `getWidgets returns existing config`() {
        whenever(widgetConfigRepository.findByUserId(userId)).thenReturn(existingConfig)
        val result = widgetService.getWidgets(userId)
        assertEquals(existingConfig, result)
        verify(widgetConfigRepository, never()).save(any())
    }

    @Test
    fun `getWidgets creates default config when none exists`() {
        whenever(widgetConfigRepository.findByUserId(userId)).thenReturn(null)
        whenever(widgetConfigRepository.save(any<WidgetConfig>())).thenAnswer { it.arguments[0] }
        val result = widgetService.getWidgets(userId)
        assertEquals(DEFAULT_WIDGETS, result.widgets)
        verify(widgetConfigRepository).save(any())
    }

    @Test
    fun `updateWidgets saves new widget order`() {
        val newWidgets = listOf(Widget("todos", "todos", true, "Aufgaben"))
        whenever(widgetConfigRepository.findByUserId(userId)).thenReturn(existingConfig)
        whenever(widgetConfigRepository.save(any<WidgetConfig>())).thenAnswer { it.arguments[0] }
        val result = widgetService.updateWidgets(userId, newWidgets)
        assertEquals(newWidgets, result.widgets)
    }

    @Test
    fun `resetWidgets restores default layout`() {
        val customConfig = existingConfig.copy(widgets = listOf(Widget("notes", "notes", false, "Notizen")))
        whenever(widgetConfigRepository.findByUserId(userId)).thenReturn(customConfig)
        whenever(widgetConfigRepository.save(any<WidgetConfig>())).thenAnswer { it.arguments[0] }
        val result = widgetService.resetWidgets(userId)
        assertEquals(DEFAULT_WIDGETS, result.widgets)
    }

    @Test
    fun `resetWidgets creates default config when none exists`() {
        whenever(widgetConfigRepository.findByUserId(userId)).thenReturn(null)
        whenever(widgetConfigRepository.save(any<WidgetConfig>())).thenAnswer { it.arguments[0] }
        val result = widgetService.resetWidgets(userId)
        assertEquals(DEFAULT_WIDGETS, result.widgets)
    }
}
