package com.example.chat.messaging.chatroom.service

import com.example.chat.messaging.user.UserModel
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap

@Service
class ChatRoomManagementService(
    private val messagingTemplate: SimpMessagingTemplate,
    private val jacksonObjectMapper: ObjectMapper,
) {

    private val logger = KotlinLogging.logger { }

    /**
     * Manages chat rooms, tracking users in each room.
     * Chat rooms are identified by a unique integer ID.
     */
    private val chatRooms: MutableMap<Int, MutableList<UserModel>> = ConcurrentHashMap()

    /**
     * Allows a user to join a chat room.
     *
     * @param user The user attempting to join a chat room. The UserModel contains user-specific data like the chat room ID.
     *             UserModel is derived from a JoinChatRoomMessageDto, representing a user's join request.
     * @throws IllegalArgumentException If the user's chat room ID is not within the valid range (1-999).
     * @throws ChatRoomFullException If the chat room the user is trying to join is already full (i.e., has 2 users).
     */
    fun join(user: UserModel) {
        val chatRoomId = user.chatRoomId
        if(chatRoomId !in 1..999) {
            return
        }

        synchronized(chatRooms) {
            val usersInRoom = chatRooms.getOrPut(chatRoomId) { mutableListOf() }

            if(usersInRoom.size >= 2) {
                // Send "chatroom full" message back to user
                logger.debug { "Chatroom $chatRoomId is full, ${user.screenName} must join another chatroom" }
                return
            }

            logger.debug { "Current users in chatroom: ${usersInRoom}, adding user ${user.screenName}" }
            usersInRoom.add(user)
            updateUserListInChatroom(user.chatRoomId)
        }
    }

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
     * @throws UserNotFoundException If the user is not found in the chat room they are attempting to leave.
     */
    fun leave(user: UserModel) {
        chatRooms[user.chatRoomId]?.remove(user).let {
            logger.debug { "Removed user ${user.screenName} from Chatroom ${user.chatRoomId}" }
        }
        updateUserListInChatroom(user.chatRoomId)
        // Notify other users in the chatroom about the disconnection
        messagingTemplate.convertAndSend(
            "/topic/chatroomUserLeave${user.chatRoomId}",
            user.screenName,
        )
    }
}