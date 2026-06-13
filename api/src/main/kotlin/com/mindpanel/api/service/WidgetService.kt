package com.mindpanel.api.service

import com.mindpanel.api.model.DEFAULT_WIDGETS
import com.mindpanel.api.model.Widget
import com.mindpanel.api.model.WidgetConfig
import com.mindpanel.api.repository.WidgetConfigRepository
import org.springframework.stereotype.Service

@Service
class WidgetService(private val widgetConfigRepository: WidgetConfigRepository) {

    fun getWidgets(userId: String): WidgetConfig {
        return widgetConfigRepository.findByUserId(userId)
            ?: widgetConfigRepository.save(WidgetConfig(userId = userId, widgets = DEFAULT_WIDGETS))
    }

    fun updateWidgets(userId: String, widgets: List<Widget>): WidgetConfig {
        val existing = widgetConfigRepository.findByUserId(userId)
        val updated = existing?.copy(widgets = widgets)
            ?: WidgetConfig(userId = userId, widgets = widgets)
        return widgetConfigRepository.save(updated)
    }

    fun resetWidgets(userId: String): WidgetConfig {
        val existing = widgetConfigRepository.findByUserId(userId)
        val reset = existing?.copy(widgets = DEFAULT_WIDGETS)
            ?: WidgetConfig(userId = userId, widgets = DEFAULT_WIDGETS)
        return widgetConfigRepository.save(reset)
    }
}
