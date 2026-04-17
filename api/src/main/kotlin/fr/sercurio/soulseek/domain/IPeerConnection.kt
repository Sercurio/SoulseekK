package fr.sercurio.soulseek.domain

import fr.sercurio.soulseek.domain.model.PeerEvent
import kotlinx.coroutines.flow.Flow

interface IPeerConnection {
  suspend fun connect()

  suspend fun disconnect()

  suspend fun send(data: ByteArray)

  fun getEvents(): Flow<PeerEvent>
}
