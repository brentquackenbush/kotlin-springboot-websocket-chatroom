package com.chatroom.springbootwebsocket.common.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

class CustomAuthenticationSuccessHandler : AuthenticationSuccessHandler {

    private val logger = KotlinLogging.logger { }
    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        val oauthToken = authentication as OAuth2AuthenticationToken
        val attributes = oauthToken.principal.attributes
        logger.debug { "Attributes received from Google: $attributes" }
        val name = attributes["name"] as String?
        val email = attributes["email"] as String?
        // Perform actions such as saving the user to the database

        response?.sendRedirect("/")
        logger.debug { "Name: $name and Email: $email" }
    }
}
