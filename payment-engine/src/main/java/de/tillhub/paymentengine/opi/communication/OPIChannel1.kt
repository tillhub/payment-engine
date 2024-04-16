package de.tillhub.paymentengine.opi.communication

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
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
            Timber.tag("OPI_CHANNEL_1").d("channel opened ${webSocket!!.isBound}")

            while (working.get()) {
                webSocket?.let {
                    try {
                        val socket = it.accept()
                        Timber.tag("OPI_CHANNEL_1").d("channel socket accepted ${socket!!.isBound}")
                        launch {
                            handleOpenConnection(socket)
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
        // for now lets play safe - would really like to see utf8 capabilities though
        val msg = message.toByteArray(CHARSET)

        coroutineScope.launch {
            Timber.tag("OPI_CHANNEL_1").d("SENT:\n$message")
            dataOutputStreams.forEach {
                it.writeInt(msg.size) // this needs additional checking for htonl() and friends
                it.write(msg)
                it.flush()
            }
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
                val length = dataInputStream.available()
                when {
                    length > PAYLOAD_SIZE_LIMIT -> {
                        val bytes = ByteArray(length)
                        dataInputStream.read(bytes)

                        val sliced = bytes.slice(IntRange(PAYLOAD_OFFSET, length-1)).toByteArray()
                        val message = String(sliced, CHARSET)

                        Timber.tag("OPI_CHANNEL_1").d("MSG RECEIVED:\n$message")
                        onMessage(message)
                    }
                    length == 0 -> Unit
                    else -> {
                        val bytes = ByteArray(length)
                        dataInputStream.read(bytes)
                        val message = String(bytes, CHARSET)

                        Timber.tag("OPI_CHANNEL_1").d("MSG DATA:\n$bytes\n$message")
                    }
                }
            } catch (e: IOException) {
                dataInputStream.close()
                dataOutputStream.close()
                socket.close()
                onError(e, "Socket message read failed")
                return@withContext
            }
        }

        dataOutputStreams.remove(dataOutputStream)

        dataInputStream.close()
        dataOutputStream.close()

        socket.close()
    }

    companion object {
        private const val DEFAULT_DELAY = 50L
        private const val PAYLOAD_SIZE_LIMIT = 7
        private const val PAYLOAD_OFFSET = 4

        // iOS uses this exclusively (the xml says something else ;-))
        private val CHARSET = Charsets.ISO_8859_1
    }
}