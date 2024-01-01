package com.chatroom.springbootwebsocket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
class WebSocketApplication

fun main(args: Array<String>) {
	runApplication<WebSocketApplication>(*args)
}
