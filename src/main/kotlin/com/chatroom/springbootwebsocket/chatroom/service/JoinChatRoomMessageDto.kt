package com.chatroom.springbootwebsocket.chatroom.service

import java.time.Instant

/**
 * Data Transfer Object for user requests to join a chat room.
 *
 * @param userId The unique identifier of the user trying to join a chat room.
 * @param chatRoomId The ID of the chat room that the user wants to join.
 * @param screenName The display name of the user within the chat room.
 * @param timestamp The timestamp marking the instant the user requested to join the chat room.
 */
data class JoinChatRoomMessageDto(
    val userId: String,
    val chatRoomId: Int,
    val screenName: String,
    var timestamp: Instant? = null,
)
