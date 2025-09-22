package fr.sercurio.soulseek.domain.model

data class PeerConnectionInfo(
    val username: String,
    val type: String,
    val ip: String,
    val port: Int,
    val token: Int,
)
