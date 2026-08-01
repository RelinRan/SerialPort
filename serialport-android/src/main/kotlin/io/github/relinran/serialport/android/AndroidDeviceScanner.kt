package io.github.relinran.serialport.android

import android.content.Context
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class SerialDevice(val path: String, val readable: Boolean, val writable: Boolean)

class AndroidDeviceScanner(private val context: Context) {
    fun scan(): Flow<List<SerialDevice>> = flow {
        val candidates = sequenceOf("/dev/ttyS", "/dev/ttyUSB", "/dev/ttyACM")
            .flatMap { prefix -> (0..31).asSequence().map { "$prefix$it" } }
            .map(::File).filter(File::exists)
            .map { SerialDevice(it.absolutePath, it.canRead(), it.canWrite()) }.toList()
        emit(candidates)
    }
}

sealed interface PermissionDiagnostic {
    data object Available : PermissionDiagnostic
    data object Missing : PermissionDiagnostic
    data object ReadDenied : PermissionDiagnostic
    data object WriteDenied : PermissionDiagnostic
}
