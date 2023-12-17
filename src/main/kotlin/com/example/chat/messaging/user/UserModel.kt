package com.example.chat.messaging.user

import com.example.chat.messaging.chatroom.service.JoinChatRoomMessageDto
import java.time.Instant
import java.time.Instant.now
import java.util.UUID

data class UserModel(
    val id: String,
    val screenName: String,
    val chatRoomId: Int,
    val timestamp: Instant
) {
    companion object {
        /**
         * Transform [JoinChatRoomMessageDto] into [UserModel] for session and chat room management.
         */
        fun fromJoinChatRoomMessageDto(dto: JoinChatRoomMessageDto): UserModel {
            return UserModel(
                id = UUID.randomUUID().toString(),
                screenName = dto.screenName,
                chatRoomId = dto.chatRoomId,
                timestamp = now(),
            )
        }
    }
}
