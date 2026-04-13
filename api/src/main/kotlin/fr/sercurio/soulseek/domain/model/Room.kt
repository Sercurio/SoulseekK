package fr.sercurio.soulseek.domain.model

data class Room(
    val name: String,
    val userCount: Int,
    val messages: List<RoomMessage> = emptyList(),
    val joined: Boolean = false,
)
