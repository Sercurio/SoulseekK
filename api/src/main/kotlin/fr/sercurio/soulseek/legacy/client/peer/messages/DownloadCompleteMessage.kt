package fr.sercurio.soulseek.legacy.client.peer.messages

data class DownloadCompleteMessage(val username: String, val filepath: String, val file: ByteArray)
