package de.fabmax.kool.util

expect object BufferUtil {
    fun inflate(zipData: Uint8Buffer): Uint8Buffer

    fun deflate(data: Uint8Buffer): Uint8Buffer

    fun encodeBase64(data: Uint8Buffer): String

    fun decodeBase64(base64: String): Uint8Buffer
}

fun ByteArray.toBuffer(): Uint8Buffer {
    val buf = createUint8Buffer(size).put(this)
    buf.flip()
    return buf
}