package io.android.serial.api

import java.net.ServerSocket
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

data class ProxyConfig(val host: String = "127.0.0.1", val port: Int = 30001)
sealed interface ProxyState { data object Stopped : ProxyState; data class Running(val port: Int) : ProxyState; data class Failed(val cause: Throwable) : ProxyState }

class SerialProxy(private val session: SerialSession, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : AutoCloseable {
    private val _state = MutableStateFlow<ProxyState>(ProxyState.Stopped)
    val state: StateFlow<ProxyState> = _state.asStateFlow()
    private var job: Job? = null

    fun start(config: ProxyConfig = ProxyConfig()) {
        if (job?.isActive == true) return
        job = kotlinx.coroutines.CoroutineScope(dispatcher).launch {
            runCatching {
                ServerSocket(config.port, 50, java.net.InetAddress.getByName(config.host)).use { server ->
                    _state.value = ProxyState.Running(server.localPort)
                    while (true) {
                        val client = server.accept()
                        launch {
                            client.use { socket ->
                                val input = socket.getInputStream()
                                val output = socket.getOutputStream()
                                val forward = launch {
                                    session.events.collectLatest { event ->
                                        if (event is SerialEvent.DataReceived) {
                                            output.write(event.data); output.flush()
                                        }
                                    }
                                }
                                val buffer = ByteArray(4096)
                                while (true) {
                                    val count = input.read(buffer)
                                    if (count < 0) break
                                    session.send(SerialCommand(buffer.copyOf(count)))
                                }
                                forward.cancel()
                            }
                        }
                    }
                }
            }.onFailure { _state.value = ProxyState.Failed(it) }
        }
    }
    override fun close() { job?.cancel(); _state.value = ProxyState.Stopped }
}

