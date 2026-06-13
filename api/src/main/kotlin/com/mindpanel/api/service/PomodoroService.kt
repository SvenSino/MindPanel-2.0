package com.mindpanel.api.service

import com.mindpanel.api.model.PomodoroSettings
import com.mindpanel.api.model.PomodoroStat
import com.mindpanel.api.repository.PomodoroSettingsRepository
import com.mindpanel.api.repository.PomodoroStatRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PomodoroService(
    private val settingsRepository: PomodoroSettingsRepository,
    private val statRepository: PomodoroStatRepository
) {

    fun getSettings(userId: String): PomodoroSettings {
        return settingsRepository.findByUserId(userId)
            ?: settingsRepository.save(PomodoroSettings(userId = userId))
    }

    fun updateSettings(
        userId: String,
        focusDuration: Int,
        breakDuration: Int,
        longBreakDuration: Int,
        autoStart: Boolean
    ): PomodoroSettings {
        val existing = settingsRepository.findByUserId(userId)
        val updated = existing?.copy(
            focusDuration = focusDuration,
            breakDuration = breakDuration,
            longBreakDuration = longBreakDuration,
            autoStart = autoStart
        ) ?: PomodoroSettings(
            userId = userId,
            focusDuration = focusDuration,
            breakDuration = breakDuration,
            longBreakDuration = longBreakDuration,
            autoStart = autoStart
        )
        return settingsRepository.save(updated)
    }

    fun getStats(userId: String, from: LocalDate, to: LocalDate): Map<String, Int> {
        return statRepository.findByUserIdAndDateBetween(userId, from, to)
            .associate { it.date.toString() to it.count }
    }

    fun recordPomodoro(userId: String): PomodoroStat {
        val today = LocalDate.now()
        val existing = statRepository.findByUserIdAndDate(userId, today)
        val updated = existing?.copy(count = existing.count + 1)
            ?: PomodoroStat(userId = userId, date = today, count = 1)
        return statRepository.save(updated)
    }
}
