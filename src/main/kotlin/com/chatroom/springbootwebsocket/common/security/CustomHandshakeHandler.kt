package com.chatroom.springbootwebsocket.common.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.server.ServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

class CustomHandshakeHandler : DefaultHandshakeHandler() {

    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Principal? {
        val userId = request.uri.query.split("&")
            .map { it.split("=") }
            .firstOrNull { it[0] == "userId" }
            ?.getOrNull(1)

        logger.debug { "WebSocket connection userId: $userId" }
        return userId?.let { UserPrincipal(it) }
    }
}