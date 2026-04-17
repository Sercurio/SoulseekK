package fr.sercurio.soulseek.domain.model

data class User(
    val name: String,
    val status: Int,
    val avgspeed: Int,
    val downloadNum: Long,
    val files: Int,
    val dirs: Int,
    val slotsFree: Int,
    val countryCode: String,
)
