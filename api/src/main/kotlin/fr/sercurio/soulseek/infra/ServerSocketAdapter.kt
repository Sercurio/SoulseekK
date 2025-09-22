package fr.sercurio.soulseek.infra

import fr.sercurio.soulseek.client.shared.AbstractSocket
import fr.sercurio.soulseek.domain.model.ChatMessage
import fr.sercurio.soulseek.domain.model.LoginResult
import fr.sercurio.soulseek.domain.model.Room
import fr.sercurio.soulseek.domain.ports.output.ServerConnectionPort
import fr.sercurio.soulseek.domain.ports.output.ServerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ServerSocketAdapter(
    host: String = "server.slsknet.org",
    port: Int = 2242,
    scope: CoroutineScope,
) : AbstractSocket(host, port, scope), ServerConnectionPort {

  private val _events = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 64)

  override fun getEvents(): Flow<ServerEvent> = _events.asSharedFlow()

  override suspend fun onSocketConnected() {}

  override suspend fun whileConnected() {
    try {
      readChannel?.readAndSetMessageLength()
      val code = readChannel?.readInt()

      val event: ServerEvent? =
          when (code) {
            1 -> parseLoginResponse()
            13 -> parseSayInChatRoom()
            //            16 -> parseUserJoinedRoom()
            //            17 -> parseUserLeftRoom()
            //            18 -> parseConnectToPeer()
            64 -> parseRoomList()
            else -> null.also { println("Unknown message code: $code") }
          }

      event?.let { _events.tryEmit(it) }

      readChannel?.skipPackLeft()
    } catch (e: Exception) {
      // Gérer les erreurs de connexion, etc.
      throw e
    }
  }

  override fun onSocketDisconnected() {}

  private suspend fun parseLoginResponse(): ServerEvent {
    return if (readChannel?.readBoolean() == true) {
      val greeting = readChannel?.readString()
      readChannel?.readInt() // ip
      ServerEvent.LoginResponse(LoginResult(true, greeting))
    } else {
      val reason: String = readChannel?.readString() ?: "Unknown reason"
      ServerEvent.LoginResponse(LoginResult(false, reason))
    }
  }

  private suspend fun parseSayInChatRoom(): ServerEvent {
    val room = readChannel?.readString().orEmpty()
    val username = readChannel?.readString().orEmpty()
    val message = readChannel?.readString().orEmpty()
    return ServerEvent.MessageReceived(ChatMessage(room, username, message))
  }

  private suspend fun parseRoomList(): ServerEvent {
    val rooms = mutableListOf<Room>()
    val nbPublicRooms = readChannel?.readInt() ?: 0
    val roomNames = List(nbPublicRooms) { readChannel?.readString() }
    val roomUserCounts = List(nbPublicRooms) { readChannel?.readInt() }

    for (i in 0 until nbPublicRooms) {
      rooms.add(Room(name = roomNames[i].orEmpty(), userCount = roomUserCounts[i] ?: 0))
    }

    return ServerEvent.RoomListUpdated(rooms)
  }
}
