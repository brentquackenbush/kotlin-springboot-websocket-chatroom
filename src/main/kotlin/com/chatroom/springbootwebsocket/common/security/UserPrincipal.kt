package com.chatroom.springbootwebsocket.common.security

import java.security.Principal

data class UserPrincipal(
    val userId: String
) : Principal {
    override fun getName(): String = userId
}