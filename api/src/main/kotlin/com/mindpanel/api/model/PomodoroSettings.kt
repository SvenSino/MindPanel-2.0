package com.mindpanel.api.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pomodoro_settings")
data class PomodoroSettings(
    @Id val id: String = ObjectId.get().toHexString(),
    val userId: String,
    val focusDuration: Int = 25,
    val breakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val autoStart: Boolean = false
)
