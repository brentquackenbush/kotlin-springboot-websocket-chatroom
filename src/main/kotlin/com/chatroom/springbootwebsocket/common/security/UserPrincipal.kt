package com.chatroom.springbootwebsocket.common.security

import java.security.Principal

/**
 * Represents the identity of a user for the lifecycle of a WebSocket session.
 *
 * @param userId The unique identifier for the user. This ID is used to track the user's session.
 */
data class UserPrincipal(
    val userId: String
) : Principal {
    /**
     * Retrieves the name of this principal, which is the user's unique identifier.
     *
     * @return The userId that represents the user's identity.
     */
    override fun getName(): String = userId
}
