package com.mindpanel.api.controller

import com.mindpanel.api.model.Widget
import com.mindpanel.api.model.WidgetConfig
import com.mindpanel.api.service.WidgetService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/widgets")
class WidgetController(private val widgetService: WidgetService) {

    @GetMapping
    fun getWidgets(@AuthenticationPrincipal jwt: Jwt): WidgetConfig =
        widgetService.getWidgets(jwt.subject)

    @PutMapping
    fun updateWidgets(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody request: WidgetUpdateRequest): WidgetConfig =
        widgetService.updateWidgets(jwt.subject, request.widgets)

    @PostMapping("/reset")
    fun resetWidgets(@AuthenticationPrincipal jwt: Jwt): WidgetConfig =
        widgetService.resetWidgets(jwt.subject)
}

data class WidgetUpdateRequest(
    @field:NotEmpty(message = "Widget-Liste darf nicht leer sein")
    val widgets: List<Widget>
)
