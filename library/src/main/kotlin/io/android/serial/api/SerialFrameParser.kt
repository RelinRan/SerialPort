package io.android.serial.api

/** Incremental parser for fixed-header, length-delimited device frames. */
public class SerialFrameParser(
    private val header: ByteArray,
    private val lengthOffset: Int,
    private val lengthSize: Int = 1,
    private val lengthIncludesHeader: Boolean = false,
    private val maxFrameSize: Int = 4096,
    private val checksum: Checksum? = null
) {
    private var pending = ByteArray(0)

    init {
        require(header.isNotEmpty())
        require(lengthOffset >= 0 && lengthSize in 1..4)
        require(maxFrameSize >= header.size)
    }

    public fun offer(bytes: ByteArray): List<ByteArray> {
        pending += bytes
        val frames = mutableListOf<ByteArray>()
        while (true) {
            val start = pending.indexOfHeader(header)
            if (start < 0) { pending = pending.takeLast(header.size - 1).toByteArray(); break }
            if (start > 0) pending = pending.copyOfRange(start, pending.size)
            if (pending.size < lengthOffset + lengthSize) break
            val declared = pending.readUnsigned(lengthOffset, lengthSize)
            val frameSize = if (lengthIncludesHeader) declared else declared + header.size + lengthSize
            if (frameSize < header.size || frameSize > maxFrameSize) { pending = pending.copyOfRange(header.size, pending.size); continue }
            if (pending.size < frameSize) break
            val frame = pending.copyOf(frameSize)
            pending = pending.copyOfRange(frameSize, pending.size)
            if (checksum == null || frame.hasChecksum(checksum)) frames += frame
        }
        return frames
    }

    public fun reset() { pending = ByteArray(0) }

    private fun ByteArray.indexOfHeader(value: ByteArray): Int = indices.firstOrNull { index ->
        index + value.size <= size && copyOfRange(index, index + value.size).contentEquals(value)
    } ?: -1

    private fun ByteArray.readUnsigned(offset: Int, size: Int): Int = (0 until size).fold(0) { result, index ->
        (result shl 8) or (this[offset + index].toInt() and 0xFF)
    }
}
