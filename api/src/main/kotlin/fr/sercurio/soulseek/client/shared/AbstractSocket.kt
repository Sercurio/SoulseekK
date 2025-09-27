package fr.sercurio.soulseek.client.shared

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
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import kotlin.coroutines.cancellation.CancellationException

abstract class AbstractSocket(
    private val host: String,
    private val port: Int,
    private val scope: CoroutineScope,
) {
    private var socket: Socket? = null
    protected lateinit var readChannel: SoulInputStream
    private var writeChannel: ByteWriteChannel? = null

    private var connectionJob: Job? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    suspend fun connect() {
        if (isConnected.value) {
            println("Déjà connecté.")
            return
        }

        connectionJob =
            scope.launch(Dispatchers.IO) {
                try {
                    println("Connexion à $host:$port...")
                    val selectorManager = SelectorManager(Dispatchers.IO)
                    val newSocket = aSocket(selectorManager).tcp().connect(host, port)

                    socket = newSocket
                    readChannel = SoulInputStream(newSocket.openReadChannel())
                    writeChannel = newSocket.openWriteChannel(autoFlush = true)

                    _isConnected.value = true
                    println("Socket connectée !")

                    onSocketConnected()

                    while (isActive && _isConnected.value) {
                        whileConnected()
                    }
                } catch (e: ConnectException) {
                    println("Erreur de connexion : ${e.message}")
                    handleDisconnect()
                    throw e
                } catch (e: ClosedReceiveChannelException) {
                    println("La connexion a été fermée par le serveur.")
                    handleDisconnect()
                } catch (e: IOException) {
                    println("Erreur I/O : ${e.message}")
                    handleDisconnect()
                } catch (e: Exception) {
                    if (e is CancellationException) {
                        println("Connexion annulée.")
                    } else {
                        println("Une erreur inattendue est survenue : ${e.message}")
                    }
                    handleDisconnect()
                }
            }
    }

    suspend fun send(data: ByteArray) {
        if (!_isConnected.value) {
            println("Impossible d'envoyer les données, socket non connectée.")
            return
        }
        try {
            writeChannel?.writeFully(data)
        } catch (e: IOException) {
            println("Erreur lors de l'envoi des données: ${e.message}")
            handleDisconnect()
        }
    }

    suspend fun disconnect() {
        connectionJob?.cancelAndJoin()
    }

    private fun handleDisconnect() {
        if (!_isConnected.value) return

        println("Déconnexion...")
        _isConnected.value = false
        try {
            socket?.close()
        } catch (e: Exception) {
        }
        socket = null
        writeChannel = null
        onSocketDisconnected()
    }

    protected abstract suspend fun onSocketConnected()

    protected abstract suspend fun whileConnected()

    protected abstract fun onSocketDisconnected()
}
