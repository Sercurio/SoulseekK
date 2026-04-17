package fr.sercurio.soulseek.data.internal

import fr.sercurio.soulseek.SoulseekClient
import fr.sercurio.soulseek.data.model.ByteMessage
import fr.sercurio.soulseek.data.model.SoulFile
import fr.sercurio.soulseek.domain.IPeerManager
import fr.sercurio.soulseek.domain.IServerConnection
import fr.sercurio.soulseek.domain.model.Login
import fr.sercurio.soulseek.domain.model.PeerConnectionInfo
import fr.sercurio.soulseek.domain.model.PeerEvent
import fr.sercurio.soulseek.domain.model.Room
import fr.sercurio.soulseek.domain.model.RoomMessage
import fr.sercurio.soulseek.domain.model.SearchReply
import fr.sercurio.soulseek.domain.model.ServerEvent
import fr.sercurio.soulseek.domain.model.UserMessage
import fr.sercurio.soulseek.legacy.server.toMD5
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

internal class SoulseekClientImpl(
    private val serverConnection: IServerConnection,
    private val peerManager: IPeerManager,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SoulseekClient {
  private val _loginStatus = MutableStateFlow<Login?>(null)
  private val _rooms = MutableStateFlow<List<Room>>(emptyList())
  private val _usersMessages = MutableStateFlow<Map<String, List<UserMessage>>>(emptyMap())

  private val _searches = mutableMapOf<Int, String>()
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
    scope.launch { peerManager.allPeerEvents.collect { processPeerEvent(it) } }
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
      is ServerEvent.PeerConnectionRequest ->
          scope.launch {
            peerManager.connectToPeer(
                PeerConnectionInfo(event.username, event.type, event.ip, event.port, event.token)
            )
          }

      else -> {}
    }
  }

  private fun processPeerEvent(event: PeerEvent) {
    when (event) {
      is PeerEvent.SearchReply -> {
        println("${event.soulFiles} trouvées chez ${event.username}")

        val query = _searches[event.token] ?: return
        val key = event.token to query

        _fileSearchResults.update { currentMap ->
          val existingList = currentMap[key] ?: emptyList()
          val newReply =
              SearchReply(
                  event.username,
                  event.token,
                  event.soulFiles,
                  event.slotsFree,
                  event.avgSpeed,
                  event.queueLength,
              )
          currentMap + (key to (existingList + newReply))
        }
      }
      is PeerEvent.TransferRequest -> {
        println("transferRequest !")
        scope.launch {
          val response =
              ByteMessage()
                  .writeInt32(41)
                  .writeInt32(event.token)
                  .writeInt32(1)
                  .writeInt64(event.size)
                  .build()

          peerManager.getPeerConnection(event.username)?.send(response)
        }
      }

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
        .writeInt32(175)
        .writeStr((user + pass).toMD5())
        .writeInt32(1)
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
    _searches[token] = request
    ByteMessage().writeInt32(26).writeInt32(token).writeStr(request).build().let {
      serverConnection.send(it)
    }
  }

  override suspend fun queueUpload(username: String, soulFile: SoulFile) {
    val peerConnection = peerManager.getPeerConnection(username)

    if (peerConnection != null) {
      val message = ByteMessage().writeInt32(43).writeStr(soulFile.filename).build()

      peerConnection.send(message)
    } else {
      ByteMessage()
          .writeInt32(18)
          .writeStr(username)
          .writeInt32(1)
          .writeInt32(Random.nextInt())
          .build()
          .let { serverConnection.send(it) }

      // TODO should store the request waiting for peer connection
    }
  }

  override suspend fun disconnect() {
    serverConnection.disconnect()
  }
}
