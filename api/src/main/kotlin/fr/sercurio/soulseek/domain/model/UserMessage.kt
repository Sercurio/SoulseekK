package fr.sercurio.soulseek.domain.model

data class UserMessage(
    val id: Int,
    val timestamp: Int,
    val username: String,
    val message: String,
    val newMessage: Boolean,
)
