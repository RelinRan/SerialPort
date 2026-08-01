package io.android.serial.api

import java.nio.ByteOrder

public data class FrameConfig(
    val header: ByteArray,
    val lengthOffset: Int,
    val lengthSize: Int = 1,
    val lengthByteOrder: ByteOrder = ByteOrder.BIG_ENDIAN,
    val lengthIncludesHeader: Boolean = false,
    val footer: ByteArray = ByteArray(0),
    val checksum: Checksum? = null,
    val checksumSize: Int = checksum?.calculate(ByteArray(0))?.size ?: 0,
    val checksumOffset: Int? = null,
    val minimumFrameSize: Int = header.size,
    val maximumFrameSize: Int = 4096,
    val maximumBufferSize: Int = maximumFrameSize * 2
) {
    init {
        require(header.isNotEmpty())
        require(lengthOffset >= 0 && lengthSize in 1..4)
        require(minimumFrameSize >= header.size && maximumFrameSize >= minimumFrameSize)
        require(maximumBufferSize >= maximumFrameSize)
        require(checksumSize >= 0)
    }
}

public sealed interface FrameParseError {
    data object InvalidLength : FrameParseError
    data object Oversized : FrameParseError
    data object ChecksumMismatch : FrameParseError
    data object FooterMismatch : FrameParseError
}

public data class FrameParseResult(val frames: List<ByteArray>, val errors: List<FrameParseError>)

public class SerialFrameParser(private val config: FrameConfig) {
    private var pending = ByteArray(0)
    public var lastErrors: List<FrameParseError> = emptyList()
        private set

    public fun offer(bytes: ByteArray): List<ByteArray> = offerDetailed(bytes).frames

    public fun offerDetailed(bytes: ByteArray): FrameParseResult {
        pending = (pending + bytes).let { if (it.size > config.maximumBufferSize) it.takeLast(config.maximumBufferSize).toByteArray() else it }
        val frames = mutableListOf<ByteArray>()
        val errors = mutableListOf<FrameParseError>()
        while (true) {
            val start = pending.indexOfHeader(config.header)
            if (start < 0) { pending = pending.takeLast(config.header.size - 1).toByteArray(); break }
            if (start > 0) pending = pending.copyOfRange(start, pending.size)
            if (pending.size < config.lengthOffset + config.lengthSize) break
            val declared = pending.readUnsigned(config.lengthOffset, config.lengthSize, config.lengthByteOrder)
            val frameSize = if (config.lengthIncludesHeader) declared else declared + config.header.size + config.lengthSize
            if (frameSize < config.minimumFrameSize) { errors += FrameParseError.InvalidLength; pending = pending.copyOfRange(config.header.size, pending.size); continue }
            if (frameSize > config.maximumFrameSize) { errors += FrameParseError.Oversized; pending = pending.copyOfRange(config.header.size, pending.size); continue }
            if (pending.size < frameSize) break
            val frame = pending.copyOf(frameSize)
            pending = pending.copyOfRange(frameSize, pending.size)
            if (config.footer.isNotEmpty() && !frame.takeLast(config.footer.size).toByteArray().contentEquals(config.footer)) { errors += FrameParseError.FooterMismatch; continue }
            if (config.checksum != null && !frame.hasChecksumAt(config.checksum, config.checksumSize, config.checksumOffset)) { errors += FrameParseError.ChecksumMismatch; continue }
            frames += frame
        }
        lastErrors = errors
        return FrameParseResult(frames, errors)
    }

    public fun reset() { pending = ByteArray(0); lastErrors = emptyList() }

    private fun ByteArray.indexOfHeader(value: ByteArray): Int = indices.firstOrNull { index -> index + value.size <= size && copyOfRange(index, index + value.size).contentEquals(value) } ?: -1
    private fun ByteArray.readUnsigned(offset: Int, size: Int, order: ByteOrder): Int = if (order == ByteOrder.BIG_ENDIAN) (0 until size).fold(0) { result, index -> (result shl 8) or (this[offset + index].toInt() and 0xFF) } else (0 until size).fold(0) { result, index -> result or ((this[offset + index].toInt() and 0xFF) shl (index * 8)) }
    private fun ByteArray.hasChecksumAt(checksum: Checksum, size: Int, offset: Int?): Boolean {
        if (size == 0 || this.size < size) return false
        val position = offset ?: (this.size - size)
        if (position < 0 || position + size > this.size) return false
        val body = copyOfRange(0, position) + copyOfRange(position + size, this.size)
        return checksum.calculate(body).contentEquals(copyOfRange(position, position + size))
    }
}
