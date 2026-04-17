package fr.sercurio.soulseek.data.network

import fr.sercurio.soulseek.data.model.ByteMessage
import fr.sercurio.soulseek.data.model.SoulFile
import fr.sercurio.soulseek.data.protocol.SoulInputStream
import fr.sercurio.soulseek.domain.model.PeerConnectionInfo
import fr.sercurio.soulseek.domain.model.PeerEvent
import io.ktor.network.selector.SelectorManager
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

class PeerConnectionSocket(
    val info: PeerConnectionInfo,
    private val scope: CoroutineScope,
    selectorManager: SelectorManager,
) : BaseSocket(info.ip, info.port, scope, selectorManager) {
  private var isInitialized = false
  private val _events = MutableSharedFlow<PeerEvent>(extraBufferCapacity = 64)
  val events: SharedFlow<PeerEvent> = _events.asSharedFlow()

  override suspend fun onSocketConnected() {
    println("Connecté à ${info.username} ${info.ip}:${info.port} token=${info.token}")
    pierceFirewall(info.token)
    println("PierceFirewall envoyé à ${info.username}")
  }

  override suspend fun whileConnected() {
    try {
      //      if (!isInitialized) {
      //        val length = readChannel.readInt32()
      //        val code = readChannel.readByte().toInt()
      //        println("Peer init reçu de ${info.username}: code=$code length=$length")
      //        isInitialized = true
      //        return
      //      }

      readChannel.readAndSetMessageLength()
      val event: PeerEvent? =
          when (val code = readChannel.readInt32()) {
            1 -> parsePeerInit()
            9 -> parseFileSearchResponse()
            40 -> parseTransferRequest()
            else -> null.also { println("Unknown message code: $code") }
          }
      event?.let { _events.tryEmit(it) }
      readChannel.skipPackLeft()
    } catch (e: Exception) {
      throw e
    }
  }

  override fun onSocketDisconnected() {}

  private suspend fun parsePeerInit(): PeerEvent {
    val username = readChannel.readString()
    val type = readChannel.readString()
    val token = readChannel.readInt32()
    return PeerEvent.PeerInit(username, type, token)
  }

  private suspend fun parseFileSearchResponse(): PeerEvent {
    println("FILE RESPONSE !")
    val messageDeflated = ByteArray(readChannel.packLeft)
    readChannel.byteReadChannel.readFully(messageDeflated, 0, readChannel.packLeft)

    val inflater = Inflater()
    inflater.setInput(messageDeflated)

    val buffer = ByteArray(1024)
    val outputStream = ByteArrayOutputStream()

    while (!inflater.finished()) {
      val count = inflater.inflate(buffer)
      outputStream.write(buffer, 0, count)
    }
    val inflatedReadChannel = SoulInputStream(ByteReadChannel(outputStream.toByteArray()))

    val soulFiles = arrayListOf<SoulFile>()

    val user = inflatedReadChannel.readString()
    val token = inflatedReadChannel.readInt32()
    var path = ""
    var size = 0L
    var extension = ""
    var bitrate = 0
    var duration = 0
    var vbr = 0
    var slotsFree = false
    var avgSpeed = 0
    var queueLength = 0L
    if (true /*TODO search the ticket*/) {
      val nResults = inflatedReadChannel.readInt32()
      for (i in 0 until nResults) {
        inflatedReadChannel.readBoolean() // unused
        path = inflatedReadChannel.readString().replace("\\", "/")
        size = inflatedReadChannel.readLong()
        extension = inflatedReadChannel.readString()
        val nAttr = inflatedReadChannel.readInt32()
        for (j in 0 until nAttr) {
          when (val posAttr = inflatedReadChannel.readInt32()) {
            0 -> bitrate = inflatedReadChannel.readInt32()
            1 -> duration = inflatedReadChannel.readInt32()
            2 -> vbr = inflatedReadChannel.readInt32()
            else -> inflatedReadChannel.readInt32()
          }
        }
        var filename = ""
        var folder = ""
        var folderPath = ""
        val a = path.lastIndexOf("/")
        if (a > 0 && a < path.length) {
          filename = path.substring(a + 1)
          folderPath = path.substring(0, a)
          val s = folderPath.lastIndexOf("/")
          folder = if (s < 0) "/" else folderPath.substring(s)
        }
        soulFiles.add(
            SoulFile(
                path,
                filename,
                folderPath,
                folder,
                size,
                extension,
                bitrate,
                vbr,
                duration,
            )
        )
      }
      slotsFree = inflatedReadChannel.readBoolean()
      avgSpeed = inflatedReadChannel.readInt32()
      queueLength = inflatedReadChannel.readLong()

      readChannel.packLeft = 0
    }
    return PeerEvent.SearchReply(user, token, soulFiles, slotsFree, avgSpeed, queueLength)
  }

  suspend fun parseTransferRequest(): PeerEvent {
    val direction = readChannel.readInt32()
    val token = readChannel.readInt32()
    val path = readChannel.readString()
    var size = 0L
    if (direction == 1) {
      size = readChannel.readLong()
      println("Peer:  ${info.username} wants to send us a file: $path")
    } else {
      println("Peer: ${info.username} wants to download a file: $path")
    }
    return PeerEvent.TransferRequest(info.username, token, direction, path, size)
  }

  suspend fun pierceFirewall(token: Int) {
    send(ByteMessage().writeInt8(0).writeInt32(token).build())
  }

  suspend fun peerInit(username: String) {
    send(ByteMessage().writeInt32(1).writeStr(username).writeStr("P").raw())
  }
}
