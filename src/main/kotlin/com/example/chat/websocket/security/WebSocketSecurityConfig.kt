package com.example.chat.websocket.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager

//@Configuration
//@EnableWebSocketSecurity
//class WebSocketSecurityConfig {
//    @Bean
//    fun messageAuthorizationManager(
//        messages: MessageMatcherDelegatingAuthorizationManager.Builder
//    ): AuthorizationManager<Message<*>> {
//        messages
//            .nullDestMatcher().authenticated()
//            .simpSubscribeDestMatchers("/app/**").permitAll()
//            //.simpDestMatchers("/app/**").hasRole("USER")
//            //.simpSubscribeDestMatchers("/user/**", "/topic/friends/*").hasRole("USER")
//            .anyMessage().permitAll()
//
//        return messages.build();
//    }
//}