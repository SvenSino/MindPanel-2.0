package com.mindpanel.api.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "pomodoro_stats")
data class PomodoroStat(
    @Id val id: String = ObjectId.get().toHexString(),
    val userId: String,
    val date: LocalDate,
    val count: Int
)
