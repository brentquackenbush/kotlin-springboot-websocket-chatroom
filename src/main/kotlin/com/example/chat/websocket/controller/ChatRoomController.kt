package com.example.chat.websocket.controller

import com.example.chat.message.chatroom.chatroommessage.ChatRoomMessageDto
import com.example.chat.message.chatroom.joinchatroommessage.JoinChatRoomMessageDto
import com.example.chat.user.domain.UserModel
import com.example.chat.websocket.service.ChatRoomManagementService
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

    @MessageMapping("/chatroom")
    fun chatRoom(joinChatRoomMessageDto: JoinChatRoomMessageDto, simpMessageHeaderAccessor: SimpMessageHeaderAccessor) {
        val user = UserModel.fromJoinChatRoomMessageDto(joinChatRoomMessageDto)
        val sessionId = simpMessageHeaderAccessor.sessionId ?: return

        // Save the user session
        sessionManager.save(sessionId, user)
        logger.debug { "User $user joined with session $sessionId" }
        // Add the user into the chatroom to keep track of number of users in the chatroom.
        chatRoomManager.join(user)
    }

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