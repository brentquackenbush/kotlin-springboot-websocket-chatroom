package com.example.chat.websocket.wsconfig

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WSConfig: WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        /**
         * Set up an in-memory message broker to carry messages back to the client on destinations prefixed with "/topic"
         * It's used for broadcasting messages to all subscribed clients.
         */
        registry.enableSimpleBroker("/topic")
        /**
         * This defines the prefix for all destinations that will be handled by our app. For example, if a client sends a message to
         * "/app/chat" Spring will route this message to a @MessageMapping -annotated method in controller.
         */
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws").withSockJS()
    }
}