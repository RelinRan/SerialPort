package android.serial.port.api

interface SerialTransport {
    suspend fun open(config: SerialConfig)
    suspend fun read(buffer: ByteArray): Int
    suspend fun write(payload: ByteArray): Int
    suspend fun close()
}

