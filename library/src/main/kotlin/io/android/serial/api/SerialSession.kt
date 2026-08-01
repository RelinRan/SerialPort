package io.android.serial.api

import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SerialSession(
    private val transport: SerialTransport,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: SerialLogger = NoOpSerialLogger
) : AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _state = MutableStateFlow<SerialState>(SerialState.Idle)
    private val _events = MutableSharedFlow<SerialEvent>(extraBufferCapacity = 64)
    private var commands: Channel<QueuedCommand> = Channel(Channel.UNLIMITED)
    private var config: SerialConfig? = null
    private var reader: Job? = null
    private var writer: Job? = null
    private var terminal = AtomicBoolean(false)
    private var pendingResponse: QueuedCommand? = null

    val state: StateFlow<SerialState> = _state.asStateFlow()
    val events: SharedFlow<SerialEvent> = _events.asSharedFlow()

    suspend fun connect(newConfig: SerialConfig): Result<Unit> = withContext(dispatcher) {
        if (terminal.get()) return@withContext Result.failure(SerialException(SerialError.AlreadyClosed))
        if (_state.value is SerialState.Connected) return@withContext Result.success(Unit)
        _state.value = SerialState.Connecting
        emit(SerialEvent.StateChanged(SerialState.Connecting))
        runCatching {
            transport.open(newConfig)
            config = newConfig
            commands = Channel(newConfig.queue.capacity)
            startWorkers(newConfig)
            val connected = SerialState.Connected(newConfig)
            _state.value = connected
            emit(SerialEvent.StateChanged(connected))
        }.onFailure { failure -> fail(SerialError.OpenFailed(failure)) }
    }

    fun send(command: SerialCommand): CommandHandle {
        val deferred = CompletableDeferred<CommandResult>()
        if (terminal.get()) {
            deferred.complete(CommandResult.Failed(SerialError.AlreadyClosed))
            return CommandHandle(command.id, deferred)
        }
        if (_state.value !is SerialState.Connected) {
            deferred.complete(CommandResult.Failed(SerialError.NotConnected))
            return CommandHandle(command.id, deferred)
        }
        val queued = QueuedCommand(command, deferred)
        if (!commands.trySend(queued).isSuccess) {
            if (config?.queue?.overflow == QueueOverflow.DropOldest) {
                commands.tryReceive().getOrNull()
                if (!commands.trySend(queued).isSuccess) deferred.complete(CommandResult.Failed(SerialError.QueueFull))
            } else {
                deferred.complete(CommandResult.Failed(SerialError.QueueFull))
            }
        }
        return CommandHandle(command.id, deferred)
    }

    suspend fun disconnect() {
        if (terminal.get() || _state.value !is SerialState.Connected) return
        _state.value = SerialState.Closing(CloseReason.Requested)
        emit(SerialEvent.StateChanged(_state.value))
        reader?.cancel(); writer?.cancel()
        commands.tryReceive().getOrNull()
        transport.close()
        _state.value = SerialState.Idle
        emit(SerialEvent.StateChanged(SerialState.Idle))
    }

    override fun close() {
        if (!terminal.compareAndSet(false, true)) return
        scope.launch {
            reader?.cancel(); writer?.cancel()
            transport.close()
            commands.close()
            _state.value = SerialState.Closed
            emit(SerialEvent.StateChanged(SerialState.Closed))
            scope.coroutineContext[Job]?.cancel()
        }
    }

    private fun startWorkers(activeConfig: SerialConfig) {
        val buffer = ByteArray(activeConfig.readBufferSize)
        reader = scope.launch {
            try {
                while (true) {
                    val length = transport.read(buffer)
                    if (length > 0) {
                        val data = buffer.copyOf(length)
                        logger.log(SerialDirection.Rx, data, Instant.now())
                        val waiting = pendingResponse
                        if (waiting != null && waiting.command.responseMatcher?.matches(waiting.command.payload, data) == true) {
                            waiting.result.complete(CommandResult.Response(data))
                            pendingResponse = null
                        }
                        emit(SerialEvent.DataReceived(data))
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                fail(SerialError.ReadFailed(failure))
            }
        }
        writer = scope.launch {
            for (queued in commands) {
                val command = queued.command
                val delay = command.delay ?: activeConfig.queue.defaultWriteDelay
                kotlinx.coroutines.delay(delay.toMillis())
                val timeout = command.timeout ?: activeConfig.queue.defaultTimeout
                var result: CommandResult = CommandResult.Failed(SerialError.WriteFailed(null))
                repeat(command.maxRetries + 1) { attempt ->
                    if (result is CommandResult.Sent) return@repeat
                    if (attempt > 0) kotlinx.coroutines.delay(command.retryDelay.toMillis())
                    result = try {
                        withTimeoutOrNull(timeout.toMillis().coerceAtLeast(1)) { transport.write(command.payload) }
                            ?.also { logger.log(SerialDirection.Tx, command.payload, Instant.now()) }
                            ?.let { CommandResult.Sent(it) }
                            ?: CommandResult.TimedOut(timeout)
                    } catch (failure: CancellationException) {
                        throw failure
                    } catch (failure: Throwable) {
                        CommandResult.Failed(SerialError.WriteFailed(failure))
                    }
                }
                if (result is CommandResult.Sent) emit(SerialEvent.CommandSent(command.id, result.bytes))
                if (result is CommandResult.TimedOut) emit(SerialEvent.CommandTimedOut(command.id, command.tag, timeout.toMillis()))
                if (result !is CommandResult.Sent || command.responseMatcher == null) queued.result.complete(result)
                if (result is CommandResult.Sent && command.responseMatcher != null) {
                    pendingResponse = queued
                    val responseTimeout = command.responseTimeout ?: timeout
                    scope.launch {
                        kotlinx.coroutines.delay(responseTimeout.toMillis().coerceAtLeast(1))
                        if (pendingResponse === queued) {
                            pendingResponse = null
                            queued.result.complete(CommandResult.TimedOut(responseTimeout))
                        }
                    }
                }
            }
        }
    }

    private suspend fun fail(error: SerialError) {
        _state.value = SerialState.Failed(error, recoverable = true)
        emit(SerialEvent.ErrorRaised(error))
        emit(SerialEvent.StateChanged(_state.value))
    }

    private suspend fun emit(event: SerialEvent) { _events.emit(event) }

    private data class QueuedCommand(val command: SerialCommand, val result: CompletableDeferred<CommandResult>)
}

class SerialException(val error: SerialError) : IllegalStateException(error.toString())

