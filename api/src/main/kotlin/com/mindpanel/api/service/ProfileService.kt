package com.mindpanel.api.service

import com.mindpanel.api.model.UserProfile
import com.mindpanel.api.repository.UserProfileRepository
import org.springframework.stereotype.Service

@Service
class ProfileService(private val userProfileRepository: UserProfileRepository) {

    fun getProfile(userId: String, firstName: String, lastName: String): UserProfile {
        val existing = userProfileRepository.findByUserId(userId)
        return if (existing == null) {
            userProfileRepository.save(UserProfile(userId = userId, firstName = firstName, lastName = lastName))
        } else if (existing.firstName != firstName || existing.lastName != lastName) {
            userProfileRepository.save(existing.copy(firstName = firstName, lastName = lastName))
        } else {
            existing
        }
    }

    fun updateProfile(userId: String, street: String, zipCode: String, city: String, country: String, avatar: String?): UserProfile {
        val existing = userProfileRepository.findByUserId(userId)
        val updated = existing?.copy(street = street, zipCode = zipCode, city = city, country = country, avatar = avatar)
            ?: UserProfile(userId = userId, street = street, zipCode = zipCode, city = city, country = country, avatar = avatar)
        return userProfileRepository.save(updated)
    }
}
