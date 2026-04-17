package fr.sercurio.soulseek.domain

import fr.sercurio.soulseek.data.network.PeerConnectionSocket
import fr.sercurio.soulseek.domain.model.PeerConnectionInfo
import fr.sercurio.soulseek.domain.model.PeerEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IPeerManager {
  val allPeerEvents: SharedFlow<PeerEvent>
  val activeConnections: StateFlow<List<PeerConnectionSocket>>

  suspend fun getPeerConnection(username: String): PeerConnectionSocket?

  suspend fun connectToPeer(info: PeerConnectionInfo)

  suspend fun disconnectAll()
}
