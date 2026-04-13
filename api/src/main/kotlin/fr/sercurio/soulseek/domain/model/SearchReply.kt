package fr.sercurio.soulseek.domain.model

import fr.sercurio.soulseek.data.model.SoulFile

data class SearchReply(
    val username: String,
    val token: Int,
    val soulFiles: ArrayList<SoulFile>,
    val slotsFree: Boolean,
    val avgSpeed: Int,
    val queueLength: Long,
)
