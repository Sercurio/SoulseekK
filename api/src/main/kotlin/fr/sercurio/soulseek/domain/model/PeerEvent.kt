package fr.sercurio.soulseek.domain.model

import fr.sercurio.soulseek.data.model.SoulFile

sealed class PeerEvent {
  data class PeerInit(val username: String, val type: String, val token: Int) : PeerEvent()

  data class SearchReply(
      val username: String,
      val token: Int,
      val soulFiles: ArrayList<SoulFile>,
      val slotsFree: Boolean,
      val avgSpeed: Int,
      val queueLength: Long,
  ) : PeerEvent()

  data class TransferRequest(
      val username: String,
      val token: Int,
      val direction: Int,
      val path: String,
      val size: Long,
  ) : PeerEvent()
}
