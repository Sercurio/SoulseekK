package fr.sercurio.soulseek.domain

import fr.sercurio.soulseek.domain.model.ServerEvent
import kotlinx.coroutines.flow.Flow

interface IServerConnection {
    suspend fun connect()

    suspend fun disconnect()

    suspend fun send(data: ByteArray)

    fun getEvents(): Flow<ServerEvent>
}
