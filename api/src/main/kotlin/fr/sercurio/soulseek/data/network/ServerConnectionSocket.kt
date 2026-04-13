package fr.sercurio.soulseek.data.network

import fr.sercurio.soulseek.domain.IServerConnection
import fr.sercurio.soulseek.domain.model.Login
import fr.sercurio.soulseek.domain.model.PeerConnectionInfo
import fr.sercurio.soulseek.domain.model.Room
import fr.sercurio.soulseek.domain.model.ServerEvent
import fr.sercurio.soulseek.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ServerConnectionSocket(
    host: String = "server.slsknet.org",
    port: Int = 2242,
    scope: CoroutineScope,
) : BaseSocket(host, port, scope), IServerConnection {

  private val _events = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 64)

  override fun getEvents(): Flow<ServerEvent> = _events.asSharedFlow()

  override suspend fun onSocketConnected() {}

  override suspend fun whileConnected() {
    try {
      readChannel.readAndSetMessageLength()
      val event: ServerEvent? =
          when (val code = readChannel.readInt()) {
            1 -> parseLoginResponse()
            3 -> parseGetPeerAddress()
            5 -> parseWatchUser()
            7 -> parseGetUserStatus()
            13 -> parseSayInChatRoom()
            14 -> parseJoinRoom()
            15 -> parseLeaveRoom()
            16 -> parseUserJoinedRoom()
            17 -> parseUserLeftRoom()
            18 -> parseConnectToPeer()
            22 -> parseMessageUser()
            26 -> parseFileSearch()
            32 -> parsePing()
            41 -> parseKickedFromServer()
            54 -> parseGetRecommendations()
            56 -> parseGetGlobalRecommendations()
            57 -> parseGetUserInterests()
            64 -> parseRoomList()
            66 -> parseGlobalAdminMessage()
            69 -> parsePrivilegedUsers()
            91 -> parseAddPrivilegedUser()
            92 -> parseCheckPrivileges()
            93 -> parseSearchRequest()
            102 -> parseNetInfo()
            104 -> parseWishlistInterval()
            110 -> parseGetSimilarUsers()
            111 -> parseGetItemRecommendations()
            112 -> parseGetItemSimilarUsers()
            113 -> parseRoomTickers()
            114 -> parseRoomTickerAdd()
            115 -> parseRoomTickerRemove()
            122 -> parseUserPrivileges()
            125 -> parseAcknowledgeNotifyPrivileges()
            133 -> parsePrivateRoomUsers()
            134 -> parsePrivateRoomAddUser()
            135 -> parsePrivateRoomRemoveUser()
            139 -> parsePrivateRoomAdded()
            140 -> parsePrivateRoomRemoved()
            141 -> parsePrivateRoomToggle()
            142 -> parseNewPassword()
            143 -> parsePrivateRoomAddOperator()
            144 -> parsePrivateRoomRemoveOperator()
            145 -> parsePrivateRoomOperatorAdded()
            146 -> parsePrivateRoomOperatorRemoved()
            148 -> parsePrivateRoomOwned()
            152 -> parseGlobalRoomMessage()
            1001 -> parseCannotConnect()

            else -> null.also { println("Unknown message code: $code") }
          }

      event?.let { _events.tryEmit(it) }

      readChannel.skipPackLeft()
    } catch (e: Exception) {
      throw e
    }
  }

  override fun onSocketDisconnected() {}

  private suspend fun parseLoginResponse(): ServerEvent {
    return if (readChannel.readBoolean()) {
      val greeting = readChannel.readString()
      readChannel.readInt() // ip
      ServerEvent.Login(Login(true, greeting))
    } else {
      val reason: String = readChannel.readString()
      ServerEvent.Login(Login(false, reason))
    }
  }

  private suspend fun parseGetPeerAddress(): ServerEvent {
    val username = readChannel.readString()
    val ip = readChannel.readIp()
    val port = readChannel.readInt()

    //        return fr.sercurio.soulseek.domain.ServerEvent.kt.GetPeerAddress(username, ip, port)
    return ServerEvent.NotImplemented
  }

  private suspend fun parseWatchUser(): ServerEvent {
    val username = readChannel.readString()
    if (readChannel.readBoolean()) {
      val status = readChannel.readInt()
      val avgSpeed = readChannel.readInt()
      val downloadNum = readChannel.readLong()
      val files = readChannel.readInt()
      val dirs = readChannel.readInt()
      readChannel.readString()

      //            fr.sercurio.soulseek.domain.ServerEvent.kt.WatchUser(username, status, avgSpeed,
      // downloadNum, files,
      // dirs)
    }
    return ServerEvent.NotImplemented
  }

  private suspend fun parseGetUserStatus(): ServerEvent {
    val username = readChannel.readString()
    val status = readChannel.readInt()
    val privileged = readChannel.readBoolean()

    //        return fr.sercurio.soulseek.domain.ServerEvent.kt.GetUserStatus(username, status,
    // privileged)
    return ServerEvent.NotImplemented
  }

  private suspend fun parseSayInChatRoom(): ServerEvent {
    val room = readChannel.readString()
    val username = readChannel.readString()
    val message = readChannel.readString()
    return ServerEvent.RoomMessage(room, username, message)
  }

  private suspend fun parseRoomList(): ServerEvent {
    val rooms = mutableListOf<Room>()
    val nbPublicRooms = readChannel.readInt()
    val roomNames = List(nbPublicRooms) { readChannel.readString() }
    val roomUserCounts = List(nbPublicRooms) { readChannel.readInt() }

    for (i in 0 until nbPublicRooms) {
      rooms.add(Room(name = roomNames[i].orEmpty(), userCount = roomUserCounts[i]))
    }

    return ServerEvent.RoomList(rooms)
  }

  private suspend fun parseJoinRoom(): ServerEvent {
    val room = readChannel.readString()
    val nbUsers = readChannel.readInt()
    //    val users = arrayOfNulls<String>(nUsers)
    //    var i = 0
    //    while (i < nUsers) {
    //      users[i] = readChannel.readString()
    //      i++
    //    }
    //    readChannel.readInt()
    //    val status = IntArray(nUsers)
    //    i = 0
    //    while (i < nUsers) {
    //      status[i] = readChannel.readInt()
    //      i++
    //    }
    //    readChannel.readInt()
    //    val avgSpeed = IntArray(nUsers)
    //    val downloadNum = LongArray(nUsers)
    //    val files = IntArray(nUsers)
    //    val dirs = IntArray(nUsers)
    //    i = 0
    //    while (i < nUsers) {
    //      avgSpeed[i] = readChannel.readInt()
    //      downloadNum[i] = readChannel.readLong()
    //      files[i] = readChannel.readInt()
    //      dirs[i] = readChannel.readInt()
    //      i++
    //    }
    //    readChannel.readInt()
    //    val slotsFree = IntArray(nUsers)
    //    i = 0
    //    while (i < nUsers) {
    //      slotsFree[i] = readChannel.readInt()
    //      i++
    //    }
    //    readChannel.readInt()
    //    val userCountries = arrayOfNulls<String>(nUsers)
    //    i = 0
    //    while (i < nUsers) {
    //      userCountries[i] = readChannel.readString()
    //      i++
    //    }
    //        if (packLeft <= 0) {
    //            i = 0
    //            while (i < nUsers) {
    //                GoSeekData.newUserInRoom(users[i], room)
    //                i++
    //            }
    //            return
    //        }
    //    val owner = readChannel.readString()
    //    val nOperators = readChannel.readInt()
    //    val operators = arrayOfNulls<String>(nOperators)
    //    i = 0
    //    while (i < nOperators) {
    //      operators[i] = readChannel.readString()
    //      i++
    //    }
    return ServerEvent.JoinRoom(room, nbUsers)
  }

  private suspend fun parseLeaveRoom(): ServerEvent {
    val room = readChannel.readString()
    return ServerEvent.RoomMessage(room, "SERVER", "Leaving room")
  }

  private suspend fun parseUserJoinedRoom(): ServerEvent {
    val room = readChannel.readString()
    val username = readChannel.readString()
    val status = readChannel.readInt()
    val avgspeed = readChannel.readInt()
    val downloadNum = readChannel.readLong()
    val files = readChannel.readInt()
    val dirs = readChannel.readInt()
    val slotsFree = readChannel.readInt()
    val countryCode = readChannel.readString()

    return ServerEvent.UserJoinedRoom(
        room,
        User(username, status, avgspeed, downloadNum, files, dirs, slotsFree, countryCode),
    )
  }

  private suspend fun parseUserLeftRoom(): ServerEvent {
    val roomName = readChannel.readString()
    val username = readChannel.readString()

    return ServerEvent.RoomMessage(roomName, username, "Left the room")
  }

  private suspend fun parseConnectToPeer(): ServerEvent {
    val username = readChannel.readString()
    val type = readChannel.readString()
    val ip: String = readChannel.readIp()
    val port = readChannel.readInt()
    val token = readChannel.readInt()
    val obfuscatedPort = readChannel.readBoolean()

    return ServerEvent.PeerConnectionRequest(PeerConnectionInfo(username, type, ip, port, token))
  }

  private suspend fun parseMessageUser(): ServerEvent {
    val id = readChannel.readInt()
    val timestamp = readChannel.readInt()
    val username = readChannel.readString()
    val message = readChannel.readString()
    val newMessage = readChannel.readBoolean()
    //        sendAcknowledgePrivateMessage(ID)
    //        if (!GoSeekData.isUserIgnored(username)) {
    //            GoSeekData.newIncomingPrivateMessage(username, message, timestamp)
    //            val a: Activity = Util.uiActivity
    //            if (a != null && a.getClass() === PrivateMessageActivity::class.java) {
    //                (a as PrivateMessageActivity).requery(username)
    //            }
    //        return fr.sercurio.soulseek.domain.ServerEvent.kt.PrivateMessageReceived()
    return ServerEvent.UserMessage(id, timestamp, username, message, newMessage)
  }

  private suspend fun parseFileSearch(): ServerEvent {
    val username = readChannel.readString()
    val ticket = readChannel.readInt()
    val query = readChannel.readString()
    val time = System.currentTimeMillis()
    return ServerEvent.NotImplemented
    //        return fr.sercurio.soulseek.domain.ServerEvent.kt.FileSearch(username, ticket, query,
    // time)

    //        val cursor: Cursor = GoSeekData.searchShares(query)
    //        println("Search Performed. query:" + query + " Time:" +
    // (System.currentTimeMillis() - time))
    //        if (cursor != null) {
    //            println("num results:" + cursor.getCount())
    //            if (cursor.getCount() !== 0) {
    //                this.service.sendToPeer(username, object : PeerMessage() {
    //
    //                    private fun send(psock: PeerSocket) {
    //                        psock.sendSearchReply(ticket, query, cursor)
    //                    }
    //                })
    //            }
    //        }
  }

  private fun parsePing(): ServerEvent = ServerEvent.NotImplemented

  //        fr.sercurio.soulseek.domain.ServerEvent.kt.Ping()

  private fun parseKickedFromServer(): ServerEvent = ServerEvent.NotImplemented

  //        fr.sercurio.soulseek.domain.ServerEvent.kt.KickedFromServer()

  private suspend fun parseGetRecommendations(): ServerEvent {
    val nRecs = readChannel.readInt()
    val recs = arrayOfNulls<String>(nRecs)
    val recLevel = IntArray(nRecs)
    var i: Int = 0
    while (i < nRecs) {
      recs[i] = readChannel.readString()
      recLevel[i] = readChannel.readInt()
      i++
    }
    val nUnRecs = readChannel.readInt()
    val unRecs = arrayOfNulls<String>(nUnRecs)
    val unRecLevel = IntArray(nUnRecs)
    i = 0
    while (i < nUnRecs) {
      unRecs[i] = readChannel.readString()
      unRecLevel[i] = readChannel.readInt()
      i++
    }

    //        return fr.sercurio.soulseek.domain.ServerEvent.kt.GetRecommendations()
    return ServerEvent.NotImplemented
  }

  private suspend fun parseGetGlobalRecommendations(): ServerEvent {
    val nRecs = readChannel.readInt()
    val recs = arrayOfNulls<String>(nRecs)
    val recLevel = IntArray(nRecs)
    var i: Int = 0
    while (i < nRecs) {
      recs[i] = readChannel.readString()
      recLevel[i] = readChannel.readInt()
      i++
    }
    val nUnRecs = readChannel.readInt()
    val unRecs = arrayOfNulls<String>(nUnRecs)
    val unRecLevel = IntArray(nUnRecs)
    i = 0
    while (i < nUnRecs) {
      unRecs[i] = readChannel.readString()
      unRecLevel[i] = readChannel.readInt()
      i++
    }
    return ServerEvent.NotImplemented
    //        return fr.sercurio.soulseek.domain.ServerEvent.kt.GetGlobalRecomendations()
  }

  private fun parseGetUserInterests(): ServerEvent {
    /*var i: Int
    val user = soulInput.readString()
    val nLikes = soulInput.readInt()
    var likes = String()
    i = 0
    while (i < nLikes) {
        likes = StringBuilder(likes).append(soulInput.readString()).append("\n").toString()
        i++
    }
    val nDislikes = soulInput.readInt()
    var dislikes = String()
    i = 0
    while (i < nDislikes) {
        dislikes = StringBuilder(dislikes).append(soulInput.readString()).append("\n").toString()
        i++
    }
    val activity: Activity = Util.uiActivity
    if (activity.getClass() === ProfileActivity::class.java && (activity as ProfileActivity).peerName.equals(user)) {
        (activity as ProfileActivity).updateLikes(likes, dislikes)
    }*/
    return ServerEvent.NotImplemented
  }

  private fun parseGlobalAdminMessage(): ServerEvent {
    // Util.toast(this, "Admin Message: " + soulInput.readString())
    return ServerEvent.NotImplemented
  }

  private fun parsePrivilegedUsers(): ServerEvent {
    /*val nUsers = soulInput.readInt()
    this.service.privilegedUsers.clear()
    for (i in 0 until nUsers) {
        this.service.privilegedUsers.add(soulInput.readString())
    }
    println( "privileged users loaded")*/
    return ServerEvent.NotImplemented
  }

  private fun parseAddPrivilegedUser(): ServerEvent {
    // this.service.privilegedUsers.add(soulInput.readString())
    return ServerEvent.NotImplemented
  }

  private suspend fun parseCheckPrivileges(): ServerEvent {
    readChannel.readInt()
    return ServerEvent.NotImplemented
  }

  private suspend fun parseSearchRequest(): ServerEvent {
    val distributedCode: Byte = readChannel.readByte()
    val unknown = readChannel.readInt()
    val username = readChannel.readString()
    val token = readChannel.readInt()
    val query = readChannel.readString()
    return ServerEvent.NotImplemented
  }

  private suspend fun parseNetInfo(): ServerEvent {
    val nParents = readChannel.readInt()
    val parentUser = arrayOfNulls<String>(nParents)
    val parentIp = arrayOfNulls<String>(nParents)
    val parentPort = IntArray(nParents)
    for (i in 0 until nParents) {
      parentUser[i] = readChannel.readString()
      parentIp[i] = readChannel.readIp()
      parentPort[i] = readChannel.readInt()
    }
    return ServerEvent.NotImplemented
  }

  private suspend fun parseWishlistInterval(): ServerEvent {
    val interval = readChannel.readInt()
    return ServerEvent.NotImplemented
  }

  private suspend fun parseGetSimilarUsers(): ServerEvent {
    val nUsers = readChannel.readInt()
    val user = arrayOfNulls<String>(nUsers)
    val status = IntArray(nUsers)
    for (i in 0 until nUsers) {
      user[i] = readChannel.readString()
      status[i] = readChannel.readInt()
    }
    return ServerEvent.NotImplemented
  }

  private suspend fun parseGetItemRecommendations(): ServerEvent {
    val item = readChannel.readString()
    val nRecs = readChannel.readInt()
    val recs = arrayOfNulls<String>(nRecs)
    val parsedValues = IntArray(nRecs)
    for (i in 0 until nRecs) {
      recs[i] = readChannel.readString()
      parsedValues[i] = readChannel.readInt()
    }
    return ServerEvent.NotImplemented
  }

  private suspend fun parseGetItemSimilarUsers(): ServerEvent {
    val item = readChannel.readString()
    val nUsers = readChannel.readInt()
    val user = arrayOfNulls<String>(nUsers)
    for (i in 0 until nUsers) {
      user[i] = readChannel.readString()
      readChannel.readInt()
    }
    return ServerEvent.NotImplemented
  }

  private fun parseRoomTickers(): ServerEvent {
    /*var i: Int
    val room = soulInput.readString()
    val nUsers = soulInput.readInt()
    val user = arrayOfNulls<String>(nUsers)
    val ticker = arrayOfNulls<String>(nUsers)
    i = 0
    while (i < nUsers) {
        user[i] = soulInput.readString()
        ticker[i] = soulInput.readString()
        i++
    }
    GoSeekData.clearRoomTickers(room)
    i = 0
    while (i < nUsers) {
        GoSeekData.newTicker(room, user[i], ticker[i])
        i++
    }*/
    return ServerEvent.NotImplemented
  }

  private fun parseRoomTickerAdd(): ServerEvent {
    // GoSeekData.newTicker(soulInput.readString(), soulInput.readString(),
    // soulInput.readString())
    return ServerEvent.NotImplemented
  }

  private fun parseRoomTickerRemove(): ServerEvent {
    // GoSeekData.removeTicker(soulInput.readString(), soulInput.readString())
    return ServerEvent.NotImplemented
  }

  private fun parseUserPrivileges(): ServerEvent {
    /* val user = soulInput.readString()
    if (soulInput.readBoolean()) {
        if (!this.service.privilegedUsers.contains(user)) {
            this.service.privilegedUsers.add(user)
        }
    } else if (this.service.privilegedUsers.contains(user)) {
        this.service.privilegedUsers.remove(user)
    }*/
    return ServerEvent.NotImplemented
  }

  private suspend fun parseAcknowledgeNotifyPrivileges(): ServerEvent {
    val token = readChannel.readInt()
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomUsers(): ServerEvent {
    val room = readChannel.readString()
    val nUsers = readChannel.readInt()
    val users = arrayOfNulls<String>(nUsers)
    for (i in 0 until nUsers) {
      users[i] = readChannel.readString()
    }
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomAddUser(): ServerEvent {
    val room = readChannel.readString()
    val user = readChannel.readString()
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomRemoveUser(): ServerEvent {
    val room = readChannel.readString()
    val user = readChannel.readString()
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomAdded(): ServerEvent {
    val room = readChannel.readString()
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomRemoved(): ServerEvent {
    val room = readChannel.readString()
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomToggle(): ServerEvent {
    val inviteEnabled = readChannel.readBoolean()
    return ServerEvent.NotImplemented
  }

  private suspend fun parseNewPassword(): ServerEvent {
    val password = readChannel.readString()
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomAddOperator(): ServerEvent {
    val room = readChannel.readString()
    val operator = readChannel.readString()
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomRemoveOperator(): ServerEvent {
    val room = readChannel.readString()
    val operator = readChannel.readString()
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomOperatorAdded(): ServerEvent {
    val room = readChannel.readString()
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomOperatorRemoved(): ServerEvent {
    val room = readChannel.readString()
    return ServerEvent.NotImplemented
  }

  private suspend fun parsePrivateRoomOwned(): ServerEvent {
    val room = readChannel.readString()
    val nOperators = readChannel.readInt()
    val operator = arrayOfNulls<String>(nOperators)
    for (i in 0 until nOperators) {
      operator[i] = readChannel.readString()
    }
    return ServerEvent.NotImplemented
  }

  private suspend fun parseGlobalRoomMessage(): ServerEvent {
    val room = readChannel.readString()
    val user = readChannel.readString()
    val message = readChannel.readString()
    return ServerEvent.RoomMessage(room, user, message)
  }

  private suspend fun parseCannotConnect(): ServerEvent {
    val token = readChannel.readInt()
    return ServerEvent.NotImplemented
  }
}
