package com.chatroom.springbootwebsocket.chatroom.controller

import com.chatroom.springbootwebsocket.chatroom.service.ChatRoomMessageDto
import com.chatroom.springbootwebsocket.chatroom.service.JoinChatRoomMessageDto
import com.chatroom.springbootwebsocket.common.user.UserModel
import com.chatroom.springbootwebsocket.chatroom.service.ChatRoomManagementService
import com.chatroom.springbootwebsocket.chatroom.service.SessionManagementService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.socket.messaging.SessionDisconnectEvent

/**
 * Controller to handle WebSocket connections and messaging for chat rooms.
 *
 * @property messagingTemplate Template for sending messages over WebSocket.
 * @property chatRoomManager Service for managing chat room logic and state.
 * @property sessionManager Service for managing user sessions.
 * @property jacksonObjectMapper Mapper for serializing and deserializing JSON.
 */
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
     * @param headerAccessor Provides access to the header accessor where session information is stored.
     */
    @MessageMapping("/chatroom")
    fun chatRoom(joinChatRoomMessageDto: JoinChatRoomMessageDto, headerAccessor: SimpMessageHeaderAccessor) {
        val sessionId = headerAccessor.sessionId ?: return
        val user = UserModel.fromJoinChatRoomMessageDto(joinChatRoomMessageDto)

        sessionManager.save(sessionId, user)
        logger.debug { "User ${user.screenName} joined with session $sessionId" }

        chatRoomManager.join(sessionId, user)
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
        val user = sessionManager.getUserFromSession(sessionId) ?: return

        logger.debug { "User ${user.screenName}: Sending message ${chatMessage.message}" }

        val jsonMessage = jacksonObjectMapper.writeValueAsString(chatMessage)
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

        sessionManager.getUserFromSession(sessionId)?.let { user ->
            // Only proceed if user is not null
            chatRoomManager.leave(user)
            sessionManager.remove(sessionId)
        }
    }
}
