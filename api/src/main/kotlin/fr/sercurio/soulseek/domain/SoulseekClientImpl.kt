package fr.sercurio.soulseek.domain

import fr.sercurio.soulseek.client.shared.model.ByteMessage
import fr.sercurio.soulseek.domain.model.ChatMessage
import fr.sercurio.soulseek.domain.model.LoginResult
import fr.sercurio.soulseek.domain.model.PeerConnectionInfo
import fr.sercurio.soulseek.domain.model.Room
import fr.sercurio.soulseek.domain.ports.input.SoulseekClient
import fr.sercurio.soulseek.domain.ports.output.ServerConnectionPort
import fr.sercurio.soulseek.domain.ports.output.ServerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SoulseekClientImpl(
    private val serverConnection: ServerConnectionPort,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SoulseekClient {
  private val _loginStatus = MutableStateFlow<LoginResult?>(null)
  private val _rooms = MutableStateFlow<List<Room>>(emptyList())
  private val _roomMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
  private val _peerConnectionRequests = MutableStateFlow<PeerConnectionInfo?>(null)

  override val loginStatus: StateFlow<LoginResult?> = _loginStatus.asStateFlow()
  override val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()
  override val roomMessages: StateFlow<Map<String, List<ChatMessage>>> = _roomMessages.asStateFlow()
  override val peerConnectionRequests: StateFlow<PeerConnectionInfo?> =
      _peerConnectionRequests.asStateFlow()

  init {
    scope.launch { serverConnection.getEvents().collect { event -> processServerEvent(event) } }
  }

  private fun processServerEvent(event: ServerEvent) {
    when (event) {
      is ServerEvent.LoginResponse -> _loginStatus.value = event.result
      is ServerEvent.RoomListUpdated -> _rooms.value = event.rooms
      is ServerEvent.MessageReceived -> {
        val currentMessages = _roomMessages.value.toMutableMap()
        val roomChat =
            currentMessages.getOrDefault(event.chatMessage.room, emptyList()).toMutableList()
        roomChat.add(event.chatMessage)
        currentMessages[event.chatMessage.room] = roomChat
        _roomMessages.value = currentMessages
      }
      is ServerEvent.PeerConnectionRequest -> _peerConnectionRequests.value = event.info
      else -> {}
    }
  }

  override suspend fun connect() {
    serverConnection.connect()
  }

  override suspend fun disconnect() {
    serverConnection.disconnect()
  }

  override suspend fun login(user: String, pass: String) {
    val loginMessage =
        ByteMessage()
            .writeInt32(1)
            .writeStr(user)
            .writeStr(pass)
            .writeInt32(160) // Version
            .getBuff()
    serverConnection.send(loginMessage)
  }

  override suspend fun joinRoom(roomName: String) {
    val joinRoomMessage = ByteMessage().writeInt32(14).writeStr(roomName).getBuff()
    serverConnection.send(joinRoomMessage)
  }

  override suspend fun sendMessage(roomName: String, message: String) {
    val sendMessage = ByteMessage().writeInt32(13).writeStr(roomName).writeStr(message).getBuff()
    serverConnection.send(sendMessage)
  }
}
