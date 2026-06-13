package com.mindpanel.api.controller

import com.mindpanel.api.model.UserProfile
import com.mindpanel.api.repository.UserProfileRepository
import com.mindpanel.api.service.ProfileService
import org.springframework.web.bind.annotation.*

data class AdminProfileUpdateRequest(
    val street: String,
    val zipCode: String,
    val city: String,
    val country: String,
    val avatar: String?
)

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val userProfileRepository: UserProfileRepository,
    private val profileService: ProfileService
) {

    @GetMapping("/users")
    fun getAllUsers(): List<UserProfile> {
        return userProfileRepository.findAll()
    }

    @PutMapping("/users/{userId}")
    fun updateUser(
        @PathVariable userId: String,
        @RequestBody request: AdminProfileUpdateRequest
    ): UserProfile {
        return profileService.updateProfile(userId, request.street, request.zipCode, request.city, request.country, request.avatar)
    }
}
