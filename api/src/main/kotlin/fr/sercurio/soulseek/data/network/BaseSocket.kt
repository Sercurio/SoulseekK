package fr.sercurio.soulseek.data.network

import fr.sercurio.soulseek.data.protocol.SoulInputStream
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

abstract class BaseSocket(
    private val host: String,
    private val port: Int,
    private val scope: CoroutineScope,
) {
  private var socket: Socket? = null
  protected lateinit var readChannel: SoulInputStream
  private var writeChannel: ByteWriteChannel? = null

  private val writeMutex = Mutex()

  private var connectionJob: Job? = null

  private val _isConnected = MutableStateFlow(false)
  val isConnected = _isConnected.asStateFlow()

  suspend fun connect() {
    if (_isConnected.value) return

    try {
      val selectorManager = SelectorManager(Dispatchers.IO)
      val newSocket = aSocket(selectorManager).tcp().connect(host, port)

      socket = newSocket
      readChannel = SoulInputStream(newSocket.openReadChannel())
      writeChannel = newSocket.openWriteChannel(autoFlush = true)

      _isConnected.value = true

      onSocketConnected()

      connectionJob =
          scope.launch(Dispatchers.IO) {
            try {
              while (isActive && _isConnected.value) {
                whileConnected()
              }
            } catch (e: Exception) {
              handleDisconnect()
            }
          }
    } catch (e: Exception) {
      println("Erreur de connexion : ${e.message}")
      handleDisconnect()
      throw e
    }
  }

  suspend fun send(data: ByteArray) {
    if (!_isConnected.value) {
      withTimeout(5000) { isConnected.first { it } }
    }

    writeMutex.withLock {
      try {
        writeChannel?.let {
          it.writeFully(data)
          it.flush()
        }
      } catch (e: Exception) {
        println("Erreur d'envoi socket: ${e.message}")
        handleDisconnect()
      }
    }
  }

  suspend fun disconnect() {
    connectionJob?.cancelAndJoin()
  }

  private fun handleDisconnect() {
    if (!_isConnected.value) return

    _isConnected.value = false
    try {
      socket?.close()
    } catch (e: Exception) {}
    socket = null
    writeChannel = null
    onSocketDisconnected()
  }

  protected abstract suspend fun onSocketConnected()

  protected abstract suspend fun whileConnected()

  protected abstract fun onSocketDisconnected()
}
