package com.example.chat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebSocketApplication

fun main(args: Array<String>) {
	runApplication<WebSocketApplication>(*args)
}