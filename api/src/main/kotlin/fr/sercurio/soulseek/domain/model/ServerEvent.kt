package fr.sercurio.soulseek.domain.model

sealed class ServerEvent {
  data class Login(val login: fr.sercurio.soulseek.domain.model.Login) : ServerEvent()

  data class RoomList(val rooms: List<Room>) : ServerEvent()

  data class RoomMessage(val roomName: String, val username: String, val message: String) :
      ServerEvent()

  data class UserMessage(
      val id: Int,
      val timestamp: Int,
      val username: String,
      val message: String,
      val newMessage: Boolean,
  ) : ServerEvent()

  data class JoinRoom(val roomName: String, val nbUsers: Int) : ServerEvent()

  data class UserJoinedRoom(val room: String, val user: User) : ServerEvent()

  data class UserLeftRoom(val room: String, val username: String) : ServerEvent()

  data class PeerConnectionRequest(
      val username: String,
      val type: String,
      val ip: String,
      val port: Int,
      val token: Int,
  ) : ServerEvent()

  object NotImplemented : ServerEvent()
}
