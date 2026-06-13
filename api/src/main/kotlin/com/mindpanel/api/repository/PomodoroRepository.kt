package com.mindpanel.api.repository

import com.mindpanel.api.model.PomodoroSettings
import com.mindpanel.api.model.PomodoroStat
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDate

interface PomodoroSettingsRepository : MongoRepository<PomodoroSettings, String> {
    fun findByUserId(userId: String): PomodoroSettings?
}

interface PomodoroStatRepository : MongoRepository<PomodoroStat, String> {
    fun findByUserIdAndDateBetween(userId: String, from: LocalDate, to: LocalDate): List<PomodoroStat>
    fun findByUserIdAndDate(userId: String, date: LocalDate): PomodoroStat?
}
