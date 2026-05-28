package com.mindpanel.api.repository

import com.mindpanel.api.model.WidgetConfig
import org.springframework.data.mongodb.repository.MongoRepository

interface WidgetConfigRepository : MongoRepository<WidgetConfig, String> {
    fun findByUserId(userId: String): WidgetConfig?
}
