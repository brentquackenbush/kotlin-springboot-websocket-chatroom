package com.example.chat.messaging.user

import com.example.chat.messaging.chatroom.service.JoinChatRoomMessageDto
import java.time.Instant
import java.time.Instant.now
import java.util.UUID

/**
 * UserModel depicting a user.
 */
data class UserModel(
    val id: String,
    // Users on-screen name
    val screenName: String,
    // The Chat Room number the user joined
    val chatRoomId: Int,
    // The time the user joins a Chat Room
    val timestamp: Instant
) {
    companion object {
        /**
         * Transform [JoinChatRoomMessageDto] into [UserModel] for session and chat room management.
         */
        fun fromJoinChatRoomMessageDto(dto: JoinChatRoomMessageDto): UserModel {
            return UserModel(
                id = dto.userId,
                screenName = dto.screenName,
                chatRoomId = dto.chatRoomId,
                timestamp = now(),
            )
        }
    }
}
