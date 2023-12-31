package com.chatroom.springbootwebsocket.common.security

import org.springframework.http.server.ServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

/**
 * Custom implementation of the DefaultHandshakeHandler to handle the WebSocket handshake.
 *
 * This class is responsible for intercepting the WebSocket handshake request and extracting
 * a user identifier to associate with the WebSocket session. This identifier is then used
 * to manage the session throughout its lifecycle.
 */
class CustomHandshakeHandler : DefaultHandshakeHandler() {

    /**
     * Determine the user associated with the WebSocket handshake.
     *
     * This method extracts the 'userId' parameter from the query string of the WebSocket handshake
     * request and uses it to create a Principal. This Principal will represent the user for the duration
     * of the WebSocket connection, allowing the server to associate messages and state with the user.
     *
     * @param request The request for the WebSocket handshake.
     * @param wsHandler The WebSocket handler that will manage the WebSocket session.
     * @param attributes The attributes from the WebSocket session.
     * @return The Principal representing the user, or null if the 'userId' cannot be extracted.
     */
    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Principal? {
        // Extract 'userId' from the query string
        val userId = request.uri.query.split("&")
            .map { it.split("=") }
            .firstOrNull { it[0] == "userId" }
            ?.getOrNull(1)

        // Return a UserPrincipal if userId is present, otherwise return null
        return userId?.let { UserPrincipal(it) }
    }
}
