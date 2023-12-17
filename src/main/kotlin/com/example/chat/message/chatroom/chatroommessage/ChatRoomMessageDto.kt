package com.example.chat.message.chatroom.chatroommessage

/**
 * Chat Message
 */
data class ChatRoomMessageDto(
    val id: String,
    val chatRoomId: Int,
    val sender: String,
    val message: String,
)