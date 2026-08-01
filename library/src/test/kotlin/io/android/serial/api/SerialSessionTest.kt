package io.android.serial.api

import java.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SerialSessionTest {
    @Test
    fun sendsInOrderAndPublishesConnectedState() = runBlocking {
        val transport = FakeTransport()
        val session = SerialSession(transport)
        val config = SerialConfig("test", 115200)

        assertEquals(Result.success(Unit), session.connect(config))
        val first = session.send(SerialCommand(byteArrayOf(1)))
        val second = session.send(SerialCommand(byteArrayOf(2)))

        assertIs<CommandResult.Sent>(first.completion.await())
        assertIs<CommandResult.Sent>(second.completion.await())
        assertEquals(listOf<Byte>(1, 2), transport.writes.flatMap { it.toList() })
        assertIs<SerialState.Connected>(session.state.value)
        session.close()
    }

    @Test
    fun timesOutSlowWrites() = runBlocking {
        val transport = FakeTransport(writeDelay = 100)
        val session = SerialSession(transport)
        session.connect(SerialConfig("test", 115200, queue = QueueConfig(defaultTimeout = Duration.ofMillis(5))))

        val result = session.send(SerialCommand(byteArrayOf(1))).completion.await()

        assertIs<CommandResult.TimedOut>(result)
        session.close()
    }

    private class FakeTransport(private val writeDelay: Long = 0) : SerialTransport {
        val writes = mutableListOf<ByteArray>()
        override suspend fun open(config: SerialConfig) = Unit
        override suspend fun read(buffer: ByteArray): Int { delay(Long.MAX_VALUE); return 0 }
        override suspend fun write(payload: ByteArray): Int { delay(writeDelay); writes += payload; return payload.size }
        override suspend fun close() = Unit
    }
}

