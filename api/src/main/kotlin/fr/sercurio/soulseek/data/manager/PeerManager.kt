package fr.sercurio.soulseek.data.manager

import fr.sercurio.soulseek.data.network.PeerConnectionSocket
import fr.sercurio.soulseek.domain.IPeerManager
import fr.sercurio.soulseek.domain.model.PeerConnectionInfo
import fr.sercurio.soulseek.domain.model.PeerEvent
import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@OptIn(ExperimentalCoroutinesApi::class)
class PeerManager(private val scope: CoroutineScope, private val selectorManager: SelectorManager) :
    IPeerManager {

  private val _activeConnections = MutableStateFlow<List<PeerConnectionSocket>>(emptyList())
  override val activeConnections: StateFlow<List<PeerConnectionSocket>> =
      _activeConnections.asStateFlow()

  private val _allPeerEvents = MutableSharedFlow<PeerEvent>(extraBufferCapacity = 128)
  override val allPeerEvents: SharedFlow<PeerEvent> = _allPeerEvents.asSharedFlow()

  override suspend fun connectToPeer(info: PeerConnectionInfo) {
    if (getPeerConnection(info.username) != null) return

    val peerSocket = PeerConnectionSocket(info, scope, selectorManager)
    _activeConnections.update { it + peerSocket }

    scope.launch {
      scope.launch {
        try {
          val collectJob = launch { peerSocket.events.collect { _allPeerEvents.emit(it) } }

          try {
            withTimeout(10000) { peerSocket.connect() }
            delay(30000)
          } finally {
            peerSocket.disconnect()
            collectJob.cancel()
          }
        } catch (e: Exception) {
          println("Échec connexion peer ${info.username}: ${e.message}")
        } finally {
          removeConnection(peerSocket)
        }
      }
    }
  }

  override suspend fun getPeerConnection(username: String): PeerConnectionSocket? =
      activeConnections.value.find { it.info.username == username }

  private fun removeConnection(socket: PeerConnectionSocket) {
    _activeConnections.update { it - socket }
  }

  override suspend fun disconnectAll() {
    _activeConnections.value.forEach {
      try {
        it.disconnect()
      } catch (e: Exception) {}
    }
    _activeConnections.value = emptyList()
  }
}
