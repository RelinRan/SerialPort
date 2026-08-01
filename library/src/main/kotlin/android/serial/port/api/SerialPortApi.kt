package android.serial.port.api

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Entry point for creating independent serial sessions. */
object SerialPortApi {
    @JvmStatic
    fun create(
        context: Context,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): SerialSession = SerialSession(AndroidSerialTransport(context.applicationContext), dispatcher)
}
