package com.mindpanel.api.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "notes")
data class Note(
    @Id val id: String = ObjectId.get().toHexString(),
    val userId: String,
    val title: String,
    val content: String,
    val archived: Boolean = false,
    val createdAt: Instant = Instant.now()
)
