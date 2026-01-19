package com.skrainyukov.testmessenger.domain.model

data class Chat(
    val id: Long,
    val name: String,
    val avatar: String? = null,
    val lastMessage: String? = null,
    val lastMessageTime: String? = null,
    val unreadCount: Int = 0
)

data class Message(
    val id: Long,
    val chatId: Long,
    val senderId: Long,
    val text: String,
    val timestamp: String,
    val isMe: Boolean
)