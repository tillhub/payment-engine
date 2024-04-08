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
import java.util.concurrent.atomic.AtomicBoolean

class OPIChannel0(
    private val socketIp: String,
    private val socketPort: Int,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private var webSocket: Socket? = null
    private var working: AtomicBoolean = AtomicBoolean(false)

    private var onMessage: ((String) -> Unit)? = null
    private var onError: (Throwable, String) -> Unit = {_,_ -> }

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
        Log.d("OPI_CHANNEL_0", "SENT:\n$message")
        coroutineScope.launch {
            Log.d("OPI_CHANNEL_0", "str: ${dataOutputStream?.toString()}")
            dataOutputStream?.writeUTF(message)
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
                if (dataInputStream!!.available() > 0) {
                    val message = dataInputStream!!.readUTF()
                    Log.d("OPI_CHANNEL_0", "MSG RECEIVED:\n$message")
                    onMessage?.invoke(message)
                }
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