package com.example.chat.messaging.chatroom.controller

import com.example.chat.messaging.chatroom.service.ChatRoomMessageDto
import com.example.chat.messaging.chatroom.service.JoinChatRoomMessageDto
import com.example.chat.messaging.user.UserModel
import com.example.chat.messaging.chatroom.service.ChatRoomManagementService
import com.example.chat.websocket.service.SessionManagementService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Controller
class ChatRoomController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val chatRoomManager: ChatRoomManagementService,
    private val sessionManager: SessionManagementService,
    private val jacksonObjectMapper: ObjectMapper,
) {

    private val logger = KotlinLogging.logger { }

    /**
     * Handles the action when a user wants to join a chatroom.
     *
     * @param joinChatRoomMessageDto DTO representing a user's request to join a chatroom.
     * @param simpMessageHeaderAccessor Provides access to the header accessor where session information is stored.
     */
    @MessageMapping("/chatroom")
    fun chatRoom(joinChatRoomMessageDto: JoinChatRoomMessageDto, simpMessageHeaderAccessor: SimpMessageHeaderAccessor) {
        val user = UserModel.fromJoinChatRoomMessageDto(joinChatRoomMessageDto)
        val sessionId = simpMessageHeaderAccessor.sessionId ?: return

        // Save the user session
        sessionManager.save(sessionId, user)

        logger.debug { "User $user joined with session $sessionId" }

        // Add the user into the chatroom to keep track of number of users in the chatroom
        chatRoomManager.join(user)
    }

    /**
     * Handles sending messages to a chatroom.
     *
     * @param chatMessage The chat message sent by the user.
     * @param headerAccessor Accessor to retrieve the user's session information.
     */
    @MessageMapping("/send")
    fun sendMessage(chatMessage: ChatRoomMessageDto, headerAccessor: SimpMessageHeaderAccessor) {
        val sessionId = headerAccessor.sessionId ?: return
        val user = sessionManager.getUserFromSession(sessionId)

        logger.debug { "User {${user.screenName}}: Sending message {${chatMessage.message}}" }

        // Map message into JSON format for the front-end to receive
        val jsonMessage = jacksonObjectMapper.writeValueAsString(chatMessage)

        // Send message to everyone in the chatroom
        messagingTemplate.convertAndSend("/topic/chatroom${chatMessage.chatRoomId}", jsonMessage)
    }

    /**
     * Listens for WebSocket disconnection events and cleans up the user session and chatroom state.
     *
     * @param event The session disconnect event.
     */
    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        val sessionId = event.sessionId

        val user = sessionManager.getUserFromSession(sessionId)
        // Remove user from the chatroom
        chatRoomManager.leave(user)
        // Remove session
        sessionManager.remove(sessionId)

        // Notify other users in the chatroom about the disconnection
        messagingTemplate.convertAndSend(
            "/topic/chatroom${user.chatRoomId}",
            "${user.screenName} has left the chatroom."
        )
    }
}