package io.github.relinran.serialport.android

import android.content.Context
import io.github.relinran.serialport.core.SerialConfig
import io.github.relinran.serialport.core.SerialError
import io.github.relinran.serialport.core.SerialTransport
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

class AndroidSerialTransport(private val context: Context) : SerialTransport {
    private var descriptor: FileDescriptor? = null
    private var input: FileInputStream? = null
    private var output: FileOutputStream? = null

    override suspend fun open(config: SerialConfig) {
        val device = File(config.path)
        if (!device.exists()) throw AndroidSerialException(SerialError.DeviceNotFound)
        if (!device.canRead() || !device.canWrite()) throw AndroidSerialException(SerialError.PermissionDenied)
        android.serial.port.api.SerialPort.open(config.path, config.baudRate, config.mode.ordinal).also { fd ->
            descriptor = fd
            input = FileInputStream(fd)
            output = FileOutputStream(fd)
        }
    }

    override suspend fun read(buffer: ByteArray): Int = input?.read(buffer) ?: throw AndroidSerialException(SerialError.NotConnected)
    override suspend fun write(payload: ByteArray): Int = output?.let { it.write(payload); payload.size } ?: throw AndroidSerialException(SerialError.NotConnected)

    override suspend fun close() {
        input?.close(); output?.close()
        descriptor = null; input = null; output = null
    }
}

class AndroidSerialException(val error: SerialError) : IllegalStateException(error.toString())
