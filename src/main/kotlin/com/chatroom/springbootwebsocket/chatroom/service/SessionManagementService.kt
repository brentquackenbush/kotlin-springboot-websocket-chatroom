package com.chatroom.springbootwebsocket.chatroom.service

import com.chatroom.springbootwebsocket.common.user.UserModel
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class SessionManagementService {

    private val sessionManagerMap : MutableMap<String, UserModel> = ConcurrentHashMap()
    private val logger = KotlinLogging.logger { }

    fun save(sessionId: String, user: UserModel) {
        sessionManagerMap[sessionId] = user
        logger.debug { "Successfully saved session and user" }
    }

    fun getUserFromSession(sessionId: String): UserModel? {
        return sessionManagerMap[sessionId]
    }

    fun remove(sessionId: String): Unit {
        sessionManagerMap.remove(sessionId).let {
            logger.debug { "Removed user session: $sessionId" }
        }
    }
}
