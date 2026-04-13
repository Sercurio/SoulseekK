package fr.sercurio.soulseek.legacy.client.peer.messages

import fr.sercurio.soulseek.data.model.SoulFile

data class SearchReplyMessage(
    val username: String,
    val token: Int,
    val soulFiles: ArrayList<SoulFile>,
    val slotsFree: Boolean,
    val avgSpeed: Int,
    val queueLength: Long,
)
