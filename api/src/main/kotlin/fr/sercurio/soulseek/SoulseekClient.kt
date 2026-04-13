package fr.sercurio.soulseek

// import fr.sercurio.soulseek.client.peer.PeerSocket
// import fr.sercurio.soulseek.client.peer.TransferSocket
// import fr.sercurio.soulseek.client.server.ServerSocket
import fr.sercurio.soulseek.data.internal.SoulseekClientImpl
import fr.sercurio.soulseek.data.network.ServerConnectionSocket
import fr.sercurio.soulseek.domain.model.Login
import fr.sercurio.soulseek.domain.model.PeerConnectionInfo
import fr.sercurio.soulseek.domain.model.Room
import fr.sercurio.soulseek.domain.model.SearchReply
import fr.sercurio.soulseek.domain.model.UserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

// data class TransferringFile(
//    val username: String,
//    val token: Int,
//    val filename: String,
//    val size: Long,
// )
//
// class SoulseekClient(
//    host: String = "server.slsknet.org",
//    port: Int = 2242,
//    private var onSearchReplyCallback: ((SearchReplyMessage) -> Unit)? = null,
//    private var onDownloadComplete: ((DownloadCompleteMessage) -> Unit)? = null,
// ) {
//  private var saveDirectory: String? = null
//
//  private val serverSocket: ServerSocket = ServerSocket(host, port)
//
//  private val peerSockets: MutableMap<String, PeerSocket> = mutableMapOf()
//  private val transferSockets: MutableMap<String, TransferSocket> = mutableMapOf()
//
//  private val fileSearches: MutableMap<Int, String> = mutableMapOf()
//  private val askedFiles: MutableMap<String, SoulFile> = mutableMapOf()
//  private val transferringFiles: MutableList<TransferringFile> = mutableListOf()
//
//  init {
//    CoroutineScope(Dispatchers.IO).launch { serverSocket.connect() }
//
//    serverSocket.onReceiveConnectToPeer { connectToPeerMessage ->
//      CoroutineScope(Dispatchers.IO).launch {
//        when (connectToPeerMessage.type) {
//          PEER.type -> {
//            val peer =
//                PeerSocket(
//                    connectToPeerMessage.ip,
//                    connectToPeerMessage.port,
//                    connectToPeerMessage.token,
//                    connectToPeerMessage.username,
//                )
//            peer.onReceiveSearchReply { onSearchReplyCallback?.invoke(it) }
//            peer.onTransferRequest { transferRequestMessage ->
//              if (askedFiles[transferRequestMessage.path] != null) {
//                println("The file is recognized as a download we requested.")
//                println("Sending a confirmation to start the transfer.")
//                transferringFiles.add(
//                    TransferringFile(
//                        peer.username,
//                        transferRequestMessage.token,
//                        transferRequestMessage.path,
//                        transferRequestMessage.size,
//                    )
//                )
//
//                CoroutineScope(Dispatchers.IO).launch {
//                  peer.downloadReply(
//                      transferRequestMessage.token,
//                      true,
//                      transferRequestMessage.size,
//                      null,
//                  )
//                }
//              } // TODO Si c'est un Trusted User accepter directement la requete
//              else {
//                launch {
//                  peer.downloadReply(
//                      transferRequestMessage.token,
//                      false,
//                      null,
//                      "Forbidden.",
//                  )
//                }
//                println("Unsolicited upload attempted at us.")
//              }
//            }
//            peerSockets[peer.username] = peer
//
//            peer.connect()
//
//            peerSockets.remove(peer.username)
//          }
//
//          TRANSFER.type -> {
//            transferringFiles
//                .find { it.username == connectToPeerMessage.username }
//                ?.let {
//                  val transfer =
//                      TransferSocket(
//                          saveDirectory,
//                          connectToPeerMessage.ip,
//                          connectToPeerMessage.port,
//                          connectToPeerMessage.username,
//                          connectToPeerMessage.token,
//                          it.filename,
//                          it.size,
//                      )
//                  transfer.onDownloadComplete { downloadCompleteMessage ->
//                    onDownloadComplete?.invoke(downloadCompleteMessage)
//                  }
//                  transferSockets[transfer.username] = transfer
//
//                  transfer.connect()
//
//                  transferSockets.remove(transfer.username)
//                }
//          }
//
//          else -> {
//            println("$connectToPeerMessage. not found on user ${connectToPeerMessage.username}")
//          }
//        }
//      }
//    }
//  }
//
//  suspend fun login(username: String, password: String) {
//    serverSocket.login(username, password)
//  }
//
//  suspend fun loginTest(username: String, password: String) =
//      serverSocket.loginTest(username, password)
//
//  suspend fun fileSearch(query: String) {
//    val token = Random.nextInt(Integer.MAX_VALUE)
//    fileSearches[token] = query
//    serverSocket.fileSearch(query, token)
//  }
//
//  suspend fun joinRoom(roomName: String) {
//    serverSocket.joinRoom(roomName)
//  }
//
//  suspend fun sayInRoom(roomName: String, message: String) {
//    serverSocket.sendRoomMessage(roomName, message)
//  }
//
//  suspend fun queueUpload(username: String, file: SoulFile) {
//    askedFiles[file.path] = file
//    peerSockets[username]?.queueUpload(file)
//  }
//
//  fun onLogin(callback: (LoginMessage) -> Unit) {
//    serverSocket.onLogin { callback(it) }
//  }
//
//  fun onReceiveRoomList(callback: (RoomListMessage) -> Unit) {
//    serverSocket.onReceiveRoomList { callback(it) }
//  }
//
//  fun onSayInRoom(callback: (SayInRoomMessage) -> Unit) {
//    serverSocket.onSayInChatRoom { callback(it) }
//  }
//
//  fun onReceiveSearchReply(callback: (SearchReplyMessage) -> Unit) {
//    onSearchReplyCallback = callback
//  }
//
//  fun onDownloadComplete(callback: (DownloadCompleteMessage) -> Unit) {
//    onDownloadComplete = callback
//  }
//
//  fun setSaveDirectory(path: String?) {
//    saveDirectory = path
//  }
// }

interface SoulseekClient {
  val login: StateFlow<Login?>
  val rooms: StateFlow<List<Room>>
  val usersMessages: StateFlow<Map<String, List<UserMessage>>>
  val fileSearchResults: StateFlow<Map<Pair<Int, String>, List<SearchReply>>>
  val peerConnectionRequests: StateFlow<PeerConnectionInfo?>

  suspend fun connect()

  suspend fun login(user: String, pass: String)

  suspend fun setWaitPort(port: Int)

  suspend fun joinRoom(roomName: String)

  suspend fun sendRoomMessage(roomName: String, message: String)

  suspend fun sendUserMessage(username: String, message: String)

  suspend fun sendUserMessageAcked(id: Int)

  suspend fun fileSearch(request: String)

  suspend fun disconnect()

  companion object {
    operator fun invoke(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    ): SoulseekClient {
      val serverPort = ServerConnectionSocket(scope = scope)
      return SoulseekClientImpl(serverPort, scope)
    }
  }
}

fun main() {
  runBlocking {
    val soulseekClient = SoulseekClient()
    soulseekClient.connect()

    soulseekClient.login("gladOS", "gladOS")

    delay(2000)

    while (true) {
      delay(Long.MAX_VALUE)
    }
  }
}
