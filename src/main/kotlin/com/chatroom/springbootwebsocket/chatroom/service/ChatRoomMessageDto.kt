package com.chatroom.springbootwebsocket.chatroom.service

/**
 * Data Transfer Object representing a message sent within a chat room.
 *
 * @param id A unique identifier for the message, a combination of timestamp and username.
 * @param chatRoomId The identifier of the chat room to which the message is sent.
 * @param sender The screen name of the user who sent the message.
 * @param message The content of the message sent by the user.
 */
data class ChatRoomMessageDto(
    val id: String,
    val chatRoomId: Int,
    val sender: String,
    val message: String,
)
