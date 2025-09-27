package fr.sercurio.soulseek.domain.ports.output

import fr.sercurio.soulseek.domain.model.ChatMessage
import fr.sercurio.soulseek.domain.model.LoginResult
import fr.sercurio.soulseek.domain.model.PeerConnectionInfo
import fr.sercurio.soulseek.domain.model.Room
import fr.sercurio.soulseek.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ServerConnectionPort {
    suspend fun connect()

    suspend fun disconnect()

    suspend fun send(data: ByteArray)

    fun getEvents(): Flow<ServerEvent>
}

sealed class ServerEvent {
    data class LoginResponse(val result: LoginResult) : ServerEvent()

    data class RoomListUpdated(val rooms: List<Room>) : ServerEvent()

    data class MessageReceived(val chatMessage: ChatMessage) : ServerEvent()

    data class UserJoinedRoom(val room: String, val user: User) : ServerEvent()

    data class UserLeftRoom(val room: String, val username: String) : ServerEvent()

    data class PeerConnectionRequest(val info: PeerConnectionInfo) : ServerEvent()

    object NotImplemented : ServerEvent()
}
