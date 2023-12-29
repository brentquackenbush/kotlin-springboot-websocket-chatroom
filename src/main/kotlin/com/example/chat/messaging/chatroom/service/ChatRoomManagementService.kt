package com.example.chat.messaging.chatroom.service

import com.example.chat.messaging.user.UserModel
import com.example.chat.websocket.service.SessionManagementService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap

/**
 * Management service that handles Chat Rooms.
 */
@Service
class ChatRoomManagementService(
    private val messagingTemplate: SimpMessagingTemplate,
    private val jacksonObjectMapper: ObjectMapper,
) {

    private val logger = KotlinLogging.logger { }

    /**
     * Manages chat rooms, tracking users in each room.
     * Chat rooms are identified by a unique integer ID representing the Chat Room ID.
     */
    private val chatRooms: MutableMap<Int, MutableList<UserModel>> = ConcurrentHashMap()

    /**
     * Allows a user to join a chat room.
     *
     * @param user The user attempting to join a chat room.
     */
    fun join(sessionId: String, user: UserModel) {
        val chatRoomId = user.chatRoomId
        if (chatRoomId !in 1..999) {
            logger.error { "Invalid chat room ID: $chatRoomId" }
            return
        }

        val usersInRoom = chatRooms.getOrPut(chatRoomId) { mutableListOf() }

        if (usersInRoom.size >= 2) {
            logger.debug { "Chatroom $chatRoomId is full, ${user.screenName} cannot join." }
            messagingTemplate.convertAndSendToUser(
                user.id, "/queue/joinResponse",
                mapOf("status" to "fail", "message" to "Chatroom is full")
            )
            return
        }

        // Add the user to the chatroom and send a join success response
        usersInRoom.add(user)
        messagingTemplate.convertAndSendToUser(
            user.id, "/queue/joinResponse",
            mapOf("status" to "success")
        )

        // Broadcast the updated user list to all users in the room
        updateUserListInChatroom(chatRoomId)
    }

    /**
     * Update the client to let them know how many users are currently in the Chat Room.
     */
    fun updateUserListInChatroom(chatRoomId: Int) {
        val usersInRoom = chatRooms[chatRoomId]?.map { it.screenName } ?: listOf()
        val usersInRoomPayload = jacksonObjectMapper.writeValueAsString(usersInRoom)

        logger.debug { "Current users in the chatroom: $usersInRoomPayload" }

        messagingTemplate.convertAndSend(
            "/topic/chatroomUsers$chatRoomId",
            usersInRoomPayload
        )
    }

    /**
     * Allows a user to leave a chat room.
     *
     * @param user The user who is leaving the chat room. The UserModel contains the chat room ID the user is part of.
     *
     * @throws UserNotFoundException If the user is not found in the chat room they are attempting to leave.
     */
    fun leave(user: UserModel) {
        chatRooms[user.chatRoomId]?.remove(user).let {
            logger.debug { "Removed user ${user.screenName} from Chatroom ${user.chatRoomId}" }
        }

        // Update UI for Users in Chatroom
        updateUserListInChatroom(user.chatRoomId)

        // Notify other users in the chatroom about the disconnection
        messagingTemplate.convertAndSend(
            "/topic/chatroomUserLeave${user.chatRoomId}",
            user.screenName,
        )
    }
}