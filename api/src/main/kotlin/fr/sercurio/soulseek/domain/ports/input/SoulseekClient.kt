package fr.sercurio.soulseek.domain.ports.input

import fr.sercurio.soulseek.domain.model.ChatMessage
import fr.sercurio.soulseek.domain.model.LoginResult
import fr.sercurio.soulseek.domain.model.PeerConnectionInfo
import fr.sercurio.soulseek.domain.model.Room
import kotlinx.coroutines.flow.StateFlow

interface SoulseekClient {
  val loginStatus: StateFlow<LoginResult?>
  val rooms: StateFlow<List<Room>>
  val roomMessages: StateFlow<Map<String, List<ChatMessage>>>
  val peerConnectionRequests: StateFlow<PeerConnectionInfo?>

  suspend fun login(user: String, pass: String)

  suspend fun joinRoom(roomName: String)

  suspend fun sendMessage(roomName: String, message: String)

  suspend fun connect()

  suspend fun disconnect()
}
