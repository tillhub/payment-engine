package de.tillhub.paymentengine.opi.communication

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal class OPIChannel0(
    private val socketIp: String,
    private val socketPort: Int,
    private val socketFactory: SocketFactory = SocketFactory(),
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private var webSocket: Socket? = null
    private var working: AtomicBoolean = AtomicBoolean(false)

    private var onMessage: (suspend (String) -> Unit)? = null
    private var onError: (Throwable, String) -> Unit = { _, _ -> }

    private var dataOutputStream: DataOutputStream? = null
    private var dataInputStream: DataInputStream? = null

    val isConnected: Boolean
        get() = webSocket != null && (webSocket?.isConnected ?: false)

    fun open() {
        if (working.get()) return

        working.set(true)

        coroutineScope.launch {
            try {
                webSocket = socketFactory.createClientSocket(socketIp, socketPort)
            } catch (e: IOException) {
                close()
                onError(e, "Terminal not reachable!")
                return@launch
            }
            Timber.tag("OPI_CHANNEL_0").d("channel opened ${webSocket!!.isConnected}")
            handleOpenConnection(webSocket!!)
        }
    }

    fun close() {
        working.set(false)

        webSocket?.close()
        webSocket = null
    }

    fun sendMessage(message: String, onResponse: suspend (String) -> Unit) {
        onMessage = onResponse

        // for now lets play safe - would really like to see utf8 capabilities though
        val msg = message.toByteArray(CHARSET)
        // this needs additional checking for htonl() and friends
        val msgSize = ByteBuffer.allocate(INT_BYTE_LENGTH).putInt(msg.size).array()

        Timber.tag("OPI_CHANNEL_0").d("SENT:\n$message")
        coroutineScope.launch {
            dataOutputStream?.write(msgSize + msg)

            dataOutputStream?.flush()
        }
    }

    fun setOnError(onError: (Throwable, String) -> Unit) {
        this.onError = onError
    }

    private suspend fun handleOpenConnection(socket: Socket) = withContext(Dispatchers.IO) {
        dataInputStream = DataInputStream(socket.getInputStream())
        dataOutputStream = DataOutputStream(socket.getOutputStream())

        var partialMsg = false
        var msgSize = 0
        var messageSB = ByteArray(0)

        while (working.get() && !socket.isClosed) {
            try {
                val length = dataInputStream!!.available()
                if (length == 0) continue

                if (partialMsg) {
                    Timber.tag("OPI_CHANNEL_0").d("PARTIAL MSG")
                    val toRead = minOf(
                        a = length,
                        b = msgSize - messageSB.size
                    )

                    val bytes = ByteArray(toRead)
                    dataInputStream!!.read(bytes, 0, toRead)

                    messageSB += bytes
                    partialMsg = msgSize != messageSB.size
                } else {
                    when {
                        length > INT_BYTE_LENGTH -> {
                            val bytes = ByteArray(length)
                            dataInputStream!!.read(bytes)

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
                    Timber.tag("OPI_CHANNEL_0").d("MSG RECEIVED:\n$message")
                    onMessage?.invoke(message)
                    msgSize = 0
                    messageSB = ByteArray(0)
                }
            } catch (e: IOException) {
                Timber.tag("OPI_CHANNEL_0").d("channel closed ${e.message}")
                dataInputStream!!.close()
                dataOutputStream!!.close()
                onError(e, "Socket message read failed")
                return@withContext
            }
        }

        dataInputStream!!.close()
        dataOutputStream!!.close()

        dataInputStream = null
        dataOutputStream = null
    }

    companion object {
        private const val INT_BYTE_LENGTH = 4

        // iOS uses ISO_8859_1 exclusively (the xml says something else ;-))
        private val CHARSET = Charsets.UTF_8
    }
}