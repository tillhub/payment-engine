package de.tillhub.paymentengine.opi.communication

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean

class OPIChannel0(
    private val socketIp: String,
    private val socketPort: Int,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private var webSocket: Socket? = null
    private var working: AtomicBoolean = AtomicBoolean(false)

    private var onMessage: ((String) -> Unit)? = null
    private var onError: (Throwable, String) -> Unit = { _, _ -> }

    private var dataOutputStream: DataOutputStream? = null
    private var dataInputStream: DataInputStream? = null

    val isConnected: Boolean
        get() = webSocket != null && (webSocket?.isConnected ?: false)

    fun open() {
        if (working.get()) return

        working.set(true)

        coroutineScope.launch {
            webSocket = Socket(socketIp, socketPort)
            Log.d("OPI_CHANNEL_0", "channel opened ${webSocket!!.isConnected}")
            handleOpenConnection(webSocket!!)
        }
    }

    fun close() {
        working.set(false)

        webSocket?.close()
        webSocket = null
    }

    fun sendMessage(message: String, onResponse: (String) -> Unit) {
        onMessage = onResponse

        val charset = Charsets.ISO_8859_1 // iOS uses this exclusively (the xml says something else ;-))
        val msg = message.toByteArray(charset) // for now lets play safe - would really like to see utf8 capabilities though

        Log.d("OPI_CHANNEL_0", "SENT:\n$msg")
        coroutineScope.launch {
            Log.d("OPI_CHANNEL_0", "str: ${dataOutputStream?.toString()}")
            dataOutputStream?.writeInt(msg.size) // this needs additional checking for htonl() and friends
            dataOutputStream?.write(msg)
            dataOutputStream?.flush()
        }
    }

    fun setOnError(onError: (Throwable, String) -> Unit) {
        this.onError = onError
    }

    private suspend fun handleOpenConnection(socket: Socket) = withContext(Dispatchers.IO) {
        dataInputStream = DataInputStream(socket.getInputStream())
        dataOutputStream = DataOutputStream(socket.getOutputStream())

        while (working.get() && !socket.isClosed) {
            try {
                val length = dataInputStream!!.available()
                when {
                    length == 4 -> { // byte length
                        dataInputStream!!.readBytes().let { bytes ->
                            val beInt = ByteBuffer.wrap(bytes).getInt()
                            val leInt = ByteBuffer.wrap(bytes).order(
                                java.nio.ByteOrder.LITTLE_ENDIAN
                            ).getInt()

                            Log.d("OPI_CHANNEL_0", "LENGTH RECEIVED:\nBE: $beInt\nLE: $leInt")
                        }
                    }
                    length > 4 -> {
                        val bytes = dataInputStream!!.readBytes()
                        val message = String(bytes, StandardCharsets.UTF_8)

                        Log.d("OPI_CHANNEL_0", "MSG RECEIVED:\n$message")
                        onMessage?.invoke(message)
                    }
                    else -> Unit
                }
//                if (dataInputStream!!.available() > 0) {
//                    val message = dataInputStream!!.readBytes()
//
//                    Log.d("OPI_CHANNEL_0", "MSG RECEIVED:\n$message")
//                    onMessage?.invoke(message.toString())
//                }
            } catch (e: IOException) {
                Log.d("OPI_CHANNEL_0", "channel closed ${e.message}")
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
}