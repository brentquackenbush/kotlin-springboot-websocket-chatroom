package com.chatroom.springbootwebsocket.chatroom.service

/**
 * Chat Message
 */
data class ChatRoomMessageDto(
    val id: String,
    val chatRoomId: Int,
    val sender: String,
    val message: String,
)