package com.mindpanel.api.repository

import com.mindpanel.api.model.UserProfile
import org.springframework.data.mongodb.repository.MongoRepository

interface UserProfileRepository : MongoRepository<UserProfile, String> {
    fun findByUserId(userId: String): UserProfile?
}
