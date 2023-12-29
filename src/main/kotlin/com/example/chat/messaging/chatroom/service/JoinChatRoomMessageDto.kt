package com.example.chat.messaging.chatroom.service

import java.time.Instant

/**
 *
 */
data class JoinChatRoomMessageDto(
    val userId: String,
    val chatRoomId: Int,
    val screenName: String,
    var timestamp: Instant? = null,
)