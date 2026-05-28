package com.mindpanel.api.controller

import com.mindpanel.api.model.UserProfile
import com.mindpanel.api.service.ProfileService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profile")
class ProfileController(private val profileService: ProfileService) {

    @GetMapping
    fun getProfile(@AuthenticationPrincipal jwt: Jwt): UserProfile =
        profileService.getProfile(jwt.subject)

    @PutMapping
    fun updateProfile(@AuthenticationPrincipal jwt: Jwt, @RequestBody request: ProfileUpdateRequest): UserProfile =
        profileService.updateProfile(jwt.subject, request.street, request.zipCode, request.city, request.country, request.avatar)
}

data class ProfileUpdateRequest(
    val street: String,
    val zipCode: String,
    val city: String,
    val country: String,
    val avatar: String?
)
