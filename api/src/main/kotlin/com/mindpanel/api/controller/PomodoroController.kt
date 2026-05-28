package com.mindpanel.api.controller

import com.mindpanel.api.model.PomodoroSettings
import com.mindpanel.api.model.PomodoroStat
import com.mindpanel.api.service.PomodoroService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/pomodoro")
class PomodoroController(private val pomodoroService: PomodoroService) {

    @GetMapping("/settings")
    fun getSettings(@AuthenticationPrincipal jwt: Jwt): PomodoroSettings =
        pomodoroService.getSettings(jwt.subject)

    @PutMapping("/settings")
    fun updateSettings(@AuthenticationPrincipal jwt: Jwt, @RequestBody request: PomodoroSettingsRequest): PomodoroSettings =
        pomodoroService.updateSettings(
            jwt.subject,
            request.focusDuration,
            request.breakDuration,
            request.longBreakDuration,
            request.autoStart
        )

    @GetMapping("/stats")
    fun getStats(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "7") days: Int
    ): Map<String, Int> {
        val to = LocalDate.now()
        val from = to.minusDays(days.toLong() - 1)
        return pomodoroService.getStats(jwt.subject, from, to)
    }

    @PostMapping("/complete")
    fun recordPomodoro(@AuthenticationPrincipal jwt: Jwt): PomodoroStat =
        pomodoroService.recordPomodoro(jwt.subject)
}

data class PomodoroSettingsRequest(
    val focusDuration: Int,
    val breakDuration: Int,
    val longBreakDuration: Int,
    val autoStart: Boolean
)
