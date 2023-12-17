package com.example.chat.websocket.service

import com.example.chat.messaging.user.UserModel
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class SessionManagementService {

    private val sessionManagerMap = mutableMapOf<String, UserModel>()
    private val logger = KotlinLogging.logger { }

    fun save(sessionId: String, user: UserModel) {
        sessionManagerMap[sessionId] = user
        logger.debug { "Successfully saved session and user" }
    }

    fun getUserFromSession(sessionId: String): UserModel {
        return sessionManagerMap[sessionId]!!
    }

    fun remove(sessionId: String): Unit {
        sessionManagerMap.remove(sessionId).let {
            logger.debug { "Removed user session: $sessionId" }
        }
    }
}