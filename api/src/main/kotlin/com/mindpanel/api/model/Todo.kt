package com.mindpanel.api.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate

@Document(collection = "todos")
data class Todo(
    @Id val id: String = ObjectId.get().toHexString(),
    val userId: String,
    val title: String,
    val completed: Boolean = false,
    val isPriority: Boolean = false,
    val dueDate: LocalDate? = null,
    val archived: Boolean = false,
    val createdAt: Instant = Instant.now()
)
