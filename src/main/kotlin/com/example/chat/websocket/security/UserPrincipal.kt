package com.example.chat.websocket.security

import java.security.Principal

data class UserPrincipal(
    val userId: String
) : Principal {
    override fun getName(): String = userId
}