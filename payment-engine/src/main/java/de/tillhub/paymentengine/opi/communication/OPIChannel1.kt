package de.tillhub.paymentengine.opi.communication

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
import java.net.SocketException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal class OPIChannel1(
    private val socketPort: Int,
    private val socketFactory: SocketFactory = SocketFactory(),
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private var webSocket: ServerSocket? = null
    private var working: AtomicBoolean = AtomicBoolean(false)

    private var dataOutputStream: DataOutputStream? = null

    private var onMessage: (String) -> Unit = {}
    private var onError: (Throwable, String) -> Unit = { _, _ -> }

    val isConnected: Boolean
        get() = webSocket != null && (webSocket?.isClosed?.not() ?: false)

    fun open() {
        if (working.get()) return

        working.set(true)
        coroutineScope.launch {
            webSocket = socketFactory.createServerSocket(socketPort)
            Timber.tag("OPI_CHANNEL_1").d("channel opened ${webSocket!!.isBound}")

            while (working.get()) {
                webSocket?.let {
                    try {
                        if (!it.isClosed) {
                            val socket = it.accept()
                            Timber.tag("OPI_CHANNEL_1")
                                .d("channel socket accepted ${socket!!.isBound}")
                            launch {
                                handleOpenConnection(socket)
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
        // for now lets play safe - would really like to see utf8 capabilities though
        val msg = message.toByteArray(CHARSET)
        // this needs additional checking for htonl() and friends
        val msgSize = ByteBuffer.allocate(INT_BYTE_LENGTH).putInt(msg.size).array()

        coroutineScope.launch {
            Timber.tag("OPI_CHANNEL_1").d("SENT:\n$message")
            dataOutputStream?.let {
                try {
                    it.write(msgSize + msg)
                    it.flush()
                } catch (_: SocketException) {}
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
        dataOutputStream = DataOutputStream(socket.getOutputStream())

        var partialMsg = false
        var msgSize = 0
        var messageSB = ByteArray(0)

        while (working.get() && !socket.isClosed) {
            try {
                val length = dataInputStream.available()
                if (length == 0) continue

                if (partialMsg) {
                    Timber.tag("OPI_CHANNEL_1").d("PARTIAL MSG")
                    val toRead = minOf(
                        a = length,
                        b = msgSize - messageSB.size
                    )

                    val bytes = ByteArray(toRead)
                    dataInputStream.read(bytes, 0, toRead)

                    messageSB += bytes
                    partialMsg = msgSize != messageSB.size
                } else {
                    when {
                        length > INT_BYTE_LENGTH -> {
                            val bytes = ByteArray(length)
                            dataInputStream.read(bytes)

                            val sliced = bytes.slice(IntRange(INT_BYTE_LENGTH, length - 1))
                                .toByteArray()

                            msgSize = ByteBuffer.allocate(INT_BYTE_LENGTH).put(
                                bytes.slice(IntRange(0, INT_BYTE_LENGTH - 1)).toByteArray()
                            ).getInt(0)

                            partialMsg = msgSize != sliced.size
                            messageSB += sliced
                        }
                        else -> Unit
                    }
                }

                if (!partialMsg) {
                    val message = String(messageSB, CHARSET)
                    Timber.tag("OPI_CHANNEL_1").d("MSG RECEIVED:\n$message")
                    onMessage(message)
                    msgSize = 0
                    messageSB = ByteArray(0)
                }
            } catch (e: IOException) {
                dataInputStream.close()
                dataOutputStream?.close()
                socket.close()
                onError(e, "Socket message read failed")
                return@withContext
            }
        }

        dataInputStream.close()
        dataOutputStream?.close()
        dataOutputStream = null

        socket.close()
    }

    companion object {
        private const val DEFAULT_DELAY = 50L
        private const val INT_BYTE_LENGTH = 4

        // iOS uses ISO_8859_1 exclusively (the xml says something else ;-))
        private val CHARSET = Charsets.UTF_8
    }
}