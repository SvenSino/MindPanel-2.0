package com.mindpanel.api.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user_profiles")
data class UserProfile(
    @Id val id: String = ObjectId.get().toHexString(),
    val userId: String,
    val firstName: String = "",
    val lastName: String = "",
    val street: String = "",
    val zipCode: String = "",
    val city: String = "",
    val country: String = "",
    val avatar: String? = null
)
