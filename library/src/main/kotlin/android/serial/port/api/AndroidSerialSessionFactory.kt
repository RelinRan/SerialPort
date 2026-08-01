package android.serial.port.api

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class AndroidSerialSessionFactory(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun create(): SerialSession = SerialSession(AndroidSerialTransport(context.applicationContext), dispatcher)
}

