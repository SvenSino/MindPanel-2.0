package com.mindpanel.api.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "widget_configs")
data class WidgetConfig(
    @Id val id: String = ObjectId.get().toHexString(),
    val userId: String,
    val widgets: List<Widget>
)

data class Widget(
    val id: String,
    val type: String,
    val enabled: Boolean,
    val title: String
)

val DEFAULT_WIDGETS = listOf(
    Widget("weather", "weather", true, "Wetter"),
    Widget("todos", "todos", true, "Aufgaben"),
    Widget("calendar", "calendar", true, "Kalender"),
    Widget("notes", "notes", true, "Notizen"),
    Widget("pomodoro", "pomodoro", false, "Pomodoro")
)
