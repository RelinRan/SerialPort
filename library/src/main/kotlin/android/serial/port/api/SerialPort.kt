package android.serial.port.api

import java.io.FileDescriptor

/** Internal JNI symbol bridge; application code uses AndroidSerialTransport. */
internal object SerialPort {
    init { System.loadLibrary("serial") }
    @JvmStatic external fun open(path: String, baudRate: Int, mode: Int): FileDescriptor
}

