package com.chatroom.springbootwebsocket.common.user

import com.chatroom.springbootwebsocket.chatroom.service.JoinChatRoomMessageDto
import java.time.Instant
import java.time.Instant.now
import java.util.UUID

/**
 * Represents a user.
 * UserModel holds essential information about a user, including their identifier, screen name,
 * the chat room they are part of, and the timestamp marking their entry into the chat room.
 *
 * @param id The unique identifier of the user. This ID is used for session tracking and message handling.
 * @param screenName The display name of the user as seen by other participants in the chat room.
 * @param chatRoomId The identifier of the chat room that the user has joined.
 *                  This information is vital for routing messages and managing chat rooms.
 * @param timestamp The time at which the user joined the chat room. This is used for logging and tracking purposes.
 */
data class UserModel(
    val id: String,
    val screenName: String,
    val chatRoomId: Int,
    val timestamp: Instant
) {
    companion object {
        /**
         * Converts a [JoinChatRoomMessageDto] to a [UserModel].
         * This transformation is crucial for creating a user instance based on the join request data.
         *
         * @param dto The [JoinChatRoomMessageDto] containing the user's request to join a chat room.
         * @return A [UserModel] instance representing the user joining the chat room.
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
