package fr.sercurio.soulseek.data.internal

import fr.sercurio.soulseek.SoulseekClient
import fr.sercurio.soulseek.data.model.ByteMessage
import fr.sercurio.soulseek.domain.IServerConnection
import fr.sercurio.soulseek.domain.model.Login
import fr.sercurio.soulseek.domain.model.PeerConnectionInfo
import fr.sercurio.soulseek.domain.model.Room
import fr.sercurio.soulseek.domain.model.RoomMessage
import fr.sercurio.soulseek.domain.model.SearchReply
import fr.sercurio.soulseek.domain.model.ServerEvent
import fr.sercurio.soulseek.domain.model.UserMessage
import fr.sercurio.soulseek.legacy.server.toMD5
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class SoulseekClientImpl(
    private val serverConnection: IServerConnection,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SoulseekClient {
  private val _loginStatus = MutableStateFlow<Login?>(null)
  private val _rooms = MutableStateFlow<List<Room>>(emptyList())
  private val _usersMessages = MutableStateFlow<Map<String, List<UserMessage>>>(emptyMap())
  private val _fileSearchResults =
      MutableStateFlow<Map<Pair<Int, String>, List<SearchReply>>>(emptyMap())
  private val _peerConnectionRequests = MutableStateFlow<PeerConnectionInfo?>(null)

  override val login = _loginStatus.asStateFlow()
  override val rooms = _rooms.asStateFlow()
  override val usersMessages = _usersMessages.asStateFlow()
  override val fileSearchResults = _fileSearchResults.asStateFlow()
  override val peerConnectionRequests = _peerConnectionRequests.asStateFlow()

  init {
    scope.launch { serverConnection.getEvents().collect { event -> processServerEvent(event) } }
  }

  private fun processServerEvent(event: ServerEvent) {
    when (event) {
      is ServerEvent.Login -> _loginStatus.value = event.login
      is ServerEvent.RoomList -> _rooms.value = event.rooms
      is ServerEvent.RoomMessage ->
          _rooms.update { rooms ->
            rooms.map { room ->
              if (room.name == event.roomName) {
                room.copy(
                    messages =
                        room.messages + RoomMessage(event.roomName, event.username, event.message)
                )
              } else {
                room
              }
            }
          }
      is ServerEvent.JoinRoom ->
          _rooms.update { rooms ->
            rooms.map { room ->
              if (room.name == event.roomName) room.copy(joined = true) else room
            }
          }
      is ServerEvent.UserMessage -> {
        _usersMessages.update { currentMap ->
          val existingMessages = currentMap[event.username] ?: emptyList()
          val newMessage =
              UserMessage(
                  event.id,
                  event.timestamp,
                  event.username,
                  event.message,
                  event.newMessage,
              )

          scope.launch { sendUserMessageAcked(event.id) }
          currentMap + (event.username to (existingMessages + newMessage))
        }
      }

      is ServerEvent.PeerConnectionRequest -> _peerConnectionRequests.value = event.info
      else -> {}
    }
  }

  override suspend fun connect() {
    withContext(Dispatchers.IO) { serverConnection.connect() }
  }

  override suspend fun login(user: String, pass: String) {
    ByteMessage()
        .writeInt32(1)
        .writeStr(user)
        .writeStr(pass)
        .writeInt32(180)
        .writeStr((user + pass).toMD5())
        .writeInt32(0)
        .build()
        .let { serverConnection.send(it) }
  }

  override suspend fun setWaitPort(port: Int) {
    ByteMessage().writeInt32(2).writeInt32(port).build().let { serverConnection.send(it) }
  }

  override suspend fun joinRoom(roomName: String) {
    ByteMessage().writeInt32(14).writeStr(roomName).build().let { serverConnection.send(it) }
  }

  override suspend fun sendRoomMessage(roomName: String, message: String) {
    ByteMessage().writeInt32(13).writeStr(roomName).writeStr(message).build().let {
      serverConnection.send(it)
    }
  }

  override suspend fun sendUserMessage(username: String, message: String) {
    ByteMessage().writeInt32(22).writeStr(username).writeStr(message).build().let {
      serverConnection.send(it)
    }
  }

  override suspend fun sendUserMessageAcked(id: Int) {
    ByteMessage().writeInt32(23).writeInt32(id).build().let { serverConnection.send(it) }
  }

  override suspend fun fileSearch(request: String) {
    val token = Random.nextInt()
    _fileSearchResults.update { currentMap -> currentMap + ((token to request) to emptyList()) }
    ByteMessage().writeInt32(token).writeStr(request).build().let { serverConnection.send(it) }
  }

  override suspend fun disconnect() {
    serverConnection.disconnect()
  }
}
