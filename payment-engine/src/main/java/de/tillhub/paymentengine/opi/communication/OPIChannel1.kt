package de.tillhub.paymentengine.opi.communication

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class OPIChannel1(
    private val socketPort: Int,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private var webSocket: ServerSocket? = null
    private var working: AtomicBoolean = AtomicBoolean(false)

    private val dataOutputStreams = mutableListOf<DataOutputStream>()

    private var onMessage: (String) -> Unit = {}
    private var onError: (Throwable, String) -> Unit = { _, _ -> }

    val isConnected: Boolean
        get() = webSocket != null && (webSocket?.isClosed?.not() ?: false)

    fun open() {
        if (working.get()) return

        working.set(true)
        coroutineScope.launch {
            webSocket = ServerSocket(socketPort)
            Log.d("OPI_CHANNEL_1", "channel opened ${webSocket!!.isBound}")

            while (working.get()) {
                webSocket?.let {
                    try {
                        it.accept().use {
                            Log.d("OPI_CHANNEL_1", "channel opened ${webSocket!!.isBound}")
                            launch {
                                handleOpenConnection(it)
                            }
                        }
                        delay(DEFAULT_DELAY)
                    } catch (e: IOException) {
                        onError(e, "Socket accept failed")
                    }
                }
            }
        }
    }

    fun close() {
        working.set(false)

        webSocket?.close()
        webSocket = null
    }

    fun sendMessage(message: String) {
        coroutineScope.launch {
            Log.d("OPI_CHANNEL_1", "SENT:\n$message")
            dataOutputStreams.forEach { it.writeUTF(message) }
        }
    }

    fun setOnMessage(onMessage: (String) -> Unit) {
        this.onMessage = onMessage
    }

    fun setOnError(onError: (Throwable, String) -> Unit) {
        this.onError = onError
    }

    private suspend fun handleOpenConnection(socket: Socket) = withContext(Dispatchers.IO) {
        val dataInputStream = DataInputStream(socket.getInputStream())
        val dataOutputStream = DataOutputStream(socket.getOutputStream())

        dataOutputStreams.add(dataOutputStream)

        while (working.get() && !socket.isClosed) {
            try {
                if (dataInputStream.available() > 0) {
                    val message = dataInputStream.readUTF()
                    Log.d("OPI_CHANNEL_1", "MSG RECEIVED:\n$message")
                    onMessage(message)
                }
            } catch (e: IOException) {
                dataInputStream.close()
                dataOutputStream.close()
                onError(e, "Socket message read failed")
                return@withContext
            }
        }

        dataOutputStreams.remove(dataOutputStream)

        dataInputStream.close()
        dataOutputStream.close()
    }

    companion object {
        private const val DEFAULT_DELAY = 500L
    }
}