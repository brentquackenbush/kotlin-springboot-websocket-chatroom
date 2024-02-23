package com.chatroom.springbootwebsocket.chatroom.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class UserProfile(
    @Id
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
) {

    fun getPictureUrl(id: String): String? = photoUrl
    fun getDisplayName(id: String): String? = name
}
