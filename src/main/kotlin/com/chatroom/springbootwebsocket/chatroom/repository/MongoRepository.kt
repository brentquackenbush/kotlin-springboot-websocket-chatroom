package com.chatroom.springbootwebsocket.chatroom.repository

import com.chatroom.springbootwebsocket.chatroom.model.UserProfile
import org.springframework.data.mongodb.repository.MongoRepository

interface UserProfileRepository : MongoRepository<UserProfile, String>
