package com.mindpanel.api.controller

import com.mindpanel.api.model.PomodoroSettings
import com.mindpanel.api.model.PomodoroStat
import com.mindpanel.api.service.PomodoroService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
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
    fun updateSettings(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody request: PomodoroSettingsRequest): PomodoroSettings =
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
    @field:Min(1, message = "Fokus-Dauer muss mindestens 1 Minute sein")
    @field:Max(120, message = "Fokus-Dauer darf maximal 120 Minuten sein")
    val focusDuration: Int,

    @field:Min(1, message = "Pause muss mindestens 1 Minute sein")
    @field:Max(60, message = "Pause darf maximal 60 Minuten sein")
    val breakDuration: Int,

    @field:Min(1, message = "Lange Pause muss mindestens 1 Minute sein")
    @field:Max(60, message = "Lange Pause darf maximal 60 Minuten sein")
    val longBreakDuration: Int,

    val autoStart: Boolean
)
