package com.chatroom.springbootwebsocket.chatroom.service

import com.chatroom.springbootwebsocket.common.user.UserModel
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap


/**
 * Service for managing chat rooms, tracking users, and broadcasting messages.
 *
 * @property messagingTemplate Template for sending messages over WebSocket to users.
 * @property jacksonObjectMapper Mapper for converting objects to JSON strings.
 */
@Service
class ChatRoomManagementService(
    private val messagingTemplate: SimpMessagingTemplate,
    private val jacksonObjectMapper: ObjectMapper,
) {
    companion object {
        const val MAX_ROOM_CAPACITY = 2
    }

    private val logger = KotlinLogging.logger { }
    private val chatRooms: MutableMap<Int, MutableList<UserModel>> = ConcurrentHashMap()

    /**
     * Joins a user to a chat room. If the room is full or the room ID is invalid,
     * it sends an error message to the user's private queue.
     *
     * @param sessionId The session ID of the user.
     * @param user The user attempting to join.
     */
    fun join(sessionId: String, user: UserModel) {
        val chatRoomId = user.chatRoomId
        validateChatRoomId(chatRoomId) ?: return

        val usersInRoom = chatRooms.getOrPut(chatRoomId) { mutableListOf() }
        if (isRoomFull(usersInRoom)) {
            sendRoomFullMessage(user)
            return
        }

        usersInRoom.add(user)
        broadcastUserListUpdate(chatRoomId)
        sendJoinSuccessMessage(user)
    }

    /**
     * Removes a user from a chat room and updates other users in the room.
     *
     * @param user The user leaving the chat room.
     */
    fun leave(user: UserModel) {
        val usersInRoom = chatRooms[user.chatRoomId]
        usersInRoom?.remove(user) ?: logger.error { "User ${user.screenName} not found in Chatroom ${user.chatRoomId}" }

        broadcastUserListUpdate(user.chatRoomId)
        notifyUserLeave(user)
    }

    // Private helper functions below
    private fun validateChatRoomId(chatRoomId: Int): Boolean? {
        if (chatRoomId !in 1..999) {
            logger.error { "Invalid chat room ID: $chatRoomId" }
            return null
        }
        return true
    }

    private fun isRoomFull(usersInRoom: MutableList<UserModel>): Boolean {
        if (usersInRoom.size >= MAX_ROOM_CAPACITY) {
            logger.debug { "Chatroom is full, cannot join." }
            return true
        }
        return false
    }

    private fun sendRoomFullMessage(user: UserModel) {
        messagingTemplate.convertAndSendToUser(
            user.id, "/queue/joinResponse",
            mapOf("status" to "fail", "message" to "Chatroom is full")
        )
    }

    private fun sendJoinSuccessMessage(user: UserModel) {
        messagingTemplate.convertAndSendToUser(
            user.id, "/queue/joinResponse",
            mapOf("status" to "success")
        )
    }

    private fun broadcastUserListUpdate(chatRoomId: Int) {
        val usersInRoom = chatRooms[chatRoomId]?.map { it.screenName } ?: listOf()
        val usersInRoomPayload = jacksonObjectMapper.writeValueAsString(usersInRoom)
        logger.debug { "Broadcasting user list update." }
        messagingTemplate.convertAndSend("/topic/chatroomUsers$chatRoomId", usersInRoomPayload)
    }

    private fun notifyUserLeave(user: UserModel) {
        logger.debug { "Notifying users about ${user.screenName} leaving chatroom ${user.chatRoomId}." }
        messagingTemplate.convertAndSend("/topic/chatroomUserLeave${user.chatRoomId}", user.screenName)
    }
}
