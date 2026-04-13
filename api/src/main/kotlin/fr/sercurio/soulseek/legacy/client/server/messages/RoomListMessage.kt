package fr.sercurio.soulseek.legacy.client.server.messages

import fr.sercurio.soulseek.legacy.server.entities.RoomApiModel

data class RoomListMessage(val rooms: List<RoomApiModel>)
