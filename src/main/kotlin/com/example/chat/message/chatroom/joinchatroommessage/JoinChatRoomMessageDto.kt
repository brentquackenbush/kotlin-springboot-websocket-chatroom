package com.example.chat.message.chatroom.joinchatroommessage

import java.time.Instant

data class JoinChatRoomMessageDto(
    val chatRoomId: Int,
    val screenName: String,
    var timestamp: Instant? = null,
)