package fr.sercurio.soulseek.legacy.client.server.messages

data class SayInRoomMessage(val room: String, val username: String, val message: String)
