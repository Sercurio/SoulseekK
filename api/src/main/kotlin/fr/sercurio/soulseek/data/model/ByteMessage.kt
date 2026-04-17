package fr.sercurio.soulseek.data.model

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class ByteMessage {
  private var data = byteArrayOf()
  private val byteOrder = ByteOrder.LITTLE_ENDIAN

  fun writeInt8(value: Int): ByteMessage {
    data += value.toByte()
    return this
  }

  fun writeInt32(value: Int): ByteMessage {
    val buffer = ByteBuffer.allocate(4).order(byteOrder).putInt(value)
    data += buffer.array()
    return this
  }

  fun writeInt64(value: Long): ByteMessage {
    val buffer = ByteBuffer.allocate(8).order(byteOrder).putLong(value)
    data += buffer.array()
    return this
  }

  fun writeStr(str: String): ByteMessage {
    val bytes = str.toByteArray(StandardCharsets.ISO_8859_1)
    writeInt32(bytes.size)
    data += bytes
    return this
  }

  fun writeBool(value: Boolean): ByteMessage {
    data += if (value) 1.toByte() else 0.toByte()
    return this
  }

  fun writeRawBytes(bytes: ByteArray): ByteMessage {
    data += bytes
    return this
  }

  fun build(): ByteArray {
    val fullBuffer = ByteBuffer.allocate(4 + data.size).order(byteOrder)
    fullBuffer.putInt(data.size)
    fullBuffer.put(data)
    return fullBuffer.array()
  }

  fun raw(): ByteArray {
    return data
  }
}
