package com.example.chat.messaging.chatroom.service

/**
 * Chat Message
 */
data class ChatRoomMessageDto(
    val id: String,
    val chatRoomId: Int,
    val sender: String,
    val message: String,
)