package com.mindpanel.api.controller

import com.mindpanel.api.model.UserProfile
import com.mindpanel.api.service.ProfileService
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profile")
class ProfileController(private val profileService: ProfileService) {

    @GetMapping
    fun getProfile(@AuthenticationPrincipal jwt: Jwt): UserProfile =
        profileService.getProfile(
            jwt.subject,
            jwt.getClaim<String>("given_name") ?: "",
            jwt.getClaim<String>("family_name") ?: ""
        )

    @PutMapping
    fun updateProfile(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody request: ProfileUpdateRequest): UserProfile =
        profileService.updateProfile(jwt.subject, request.street, request.zipCode, request.city, request.country, request.avatar)
}

data class ProfileUpdateRequest(
    @field:Size(max = 255, message = "Straße darf maximal 255 Zeichen lang sein")
    val street: String,

    @field:Size(max = 20, message = "PLZ darf maximal 20 Zeichen lang sein")
    val zipCode: String,

    @field:Size(max = 100, message = "Stadt darf maximal 100 Zeichen lang sein")
    val city: String,

    @field:Size(max = 100, message = "Land darf maximal 100 Zeichen lang sein")
    val country: String,

    val avatar: String?
)
