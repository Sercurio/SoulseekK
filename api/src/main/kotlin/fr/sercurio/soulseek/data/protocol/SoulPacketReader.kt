package fr.sercurio.soulseek.data.protocol

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.discardExact
import io.ktor.utils.io.readByte
import io.ktor.utils.io.readFully
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SoulInputStream(val byteReadChannel: ByteReadChannel) {
  var packLeft: Int = 0
  private val boolBuffer = ByteBuffer.allocate(1)
  private val intBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
  private val longBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)

  suspend fun readAndSetMessageLength() {
    packLeft = readInt32()
  }

  private fun readInt32(dis: DataInputStream): Int {
    return ByteBuffer.allocate(4)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putInt(dis.readInt())
        .order(ByteOrder.BIG_ENDIAN)
        .getInt(0)
  }

  suspend fun readInt32(): Int {
    intBuffer.clear()
    byteReadChannel.readFully(intBuffer)
    intBuffer.flip()
    val result = intBuffer.getInt()
    packLeft -= 4
    return result
  }

  suspend fun readBoolean(): Boolean {
    boolBuffer.clear()
    byteReadChannel.readFully(boolBuffer)
    boolBuffer.flip()
    val result = boolBuffer.get().toInt() != 0
    packLeft--
    return result
  }

  suspend fun readString(): String {
    val length = readInt32()
    val tmp = ByteArray(length)
    byteReadChannel.readFully(tmp, 0, length)
    packLeft -= length
    return String(tmp).replace("\\", "/")
  }

  fun readLong(dis: DataInputStream): Long {
    return ByteBuffer.allocate(8)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putLong(dis.readLong())
        .order(ByteOrder.BIG_ENDIAN)
        .getLong(0)
  }

  fun readByte(dis: DataInputStream): Byte {
    return dis.readByte()
  }

  fun readString(dis: DataInputStream): String {
    val length = readInt32(dis)
    val tmp = ByteArray(length)
    dis.readFully(tmp, 0, length)
    return String(tmp).replace("\\", "/")
  }

  suspend fun readIp(): String {
    val d: Byte = readByte()
    val c: Byte = readByte()
    val b: Byte = readByte()
    val a: Byte = readByte()
    return (if (a >= 0.toByte()) a else a + 256).toString() +
        "." +
        (if (b >= 0.toByte()) b else b + 256) +
        "." +
        (if (c >= 0.toByte()) c else c + 256) +
        "." +
        if (d >= 0.toByte()) d else d + 256
  }

  suspend fun readLong(): Long {
    longBuffer.clear()
    byteReadChannel.readFully(longBuffer)
    longBuffer.flip()
    val result = longBuffer.getLong()
    packLeft -= 8
    return result
  }

  fun readBoolean(dis: DataInputStream): Boolean {
    return dis.readBoolean()
  }

  suspend fun readByte(): Byte {
    val a = byteReadChannel.readByte()
    packLeft--
    return a
  }

  suspend fun checkPackLeft() {
    if (packLeft > 0) {
      println("Skipping bytes. N: $packLeft")
      byteReadChannel.discardExact(packLeft.toLong())
    }
    if (packLeft < 0) {
      println("Overrun on packet reading!")
      throw IOException("Overrun on packet reading!, packleft: $packLeft")
    }
  }

  suspend fun skipPackLeft() {
    byteReadChannel.discardExact(packLeft.toLong())
  }
}
