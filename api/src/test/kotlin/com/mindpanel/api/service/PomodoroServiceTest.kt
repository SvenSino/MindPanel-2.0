package com.mindpanel.api.service

import com.mindpanel.api.model.PomodoroSettings
import com.mindpanel.api.model.PomodoroStat
import com.mindpanel.api.repository.PomodoroSettingsRepository
import com.mindpanel.api.repository.PomodoroStatRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.LocalDate

class PomodoroServiceTest {

    private val settingsRepository: PomodoroSettingsRepository = mock()
    private val statRepository: PomodoroStatRepository = mock()
    private val pomodoroService = PomodoroService(settingsRepository, statRepository)

    private val userId = "user-123"

    @Test
    fun `getSettings returns existing settings`() {
        val settings = PomodoroSettings(userId = userId, focusDuration = 30)
        whenever(settingsRepository.findByUserId(userId)).thenReturn(settings)
        val result = pomodoroService.getSettings(userId)
        assertEquals(30, result.focusDuration)
    }

    @Test
    fun `getSettings creates defaults when none exist`() {
        whenever(settingsRepository.findByUserId(userId)).thenReturn(null)
        whenever(settingsRepository.save(any<PomodoroSettings>())).thenAnswer { it.arguments[0] }
        val result = pomodoroService.getSettings(userId)
        assertEquals(25, result.focusDuration)
        assertEquals(5, result.breakDuration)
        assertEquals(15, result.longBreakDuration)
    }

    @Test
    fun `updateSettings saves updated values`() {
        val existing = PomodoroSettings(userId = userId)
        whenever(settingsRepository.findByUserId(userId)).thenReturn(existing)
        whenever(settingsRepository.save(any<PomodoroSettings>())).thenAnswer { it.arguments[0] }
        val result = pomodoroService.updateSettings(userId, 45, 10, 20, true)
        assertEquals(45, result.focusDuration)
        assertEquals(10, result.breakDuration)
        assertEquals(20, result.longBreakDuration)
        assertTrue(result.autoStart)
    }

    @Test
    fun `recordPomodoro increments existing stat`() {
        val today = LocalDate.now()
        val existing = PomodoroStat(userId = userId, date = today, count = 3)
        whenever(statRepository.findByUserIdAndDate(userId, today)).thenReturn(existing)
        whenever(statRepository.save(any<PomodoroStat>())).thenAnswer { it.arguments[0] }
        val result = pomodoroService.recordPomodoro(userId)
        assertEquals(4, result.count)
    }

    @Test
    fun `recordPomodoro creates new stat for today`() {
        val today = LocalDate.now()
        whenever(statRepository.findByUserIdAndDate(userId, today)).thenReturn(null)
        whenever(statRepository.save(any<PomodoroStat>())).thenAnswer { it.arguments[0] }
        val result = pomodoroService.recordPomodoro(userId)
        assertEquals(1, result.count)
        assertEquals(today, result.date)
    }

    @Test
    fun `getStats returns map of date to count`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val stats = listOf(
            PomodoroStat(userId = userId, date = today, count = 3),
            PomodoroStat(userId = userId, date = yesterday, count = 5)
        )
        whenever(statRepository.findByUserIdAndDateBetween(eq(userId), any(), any())).thenReturn(stats)
        val result = pomodoroService.getStats(userId, yesterday, today)
        assertEquals(3, result[today.toString()])
        assertEquals(5, result[yesterday.toString()])
    }
}
