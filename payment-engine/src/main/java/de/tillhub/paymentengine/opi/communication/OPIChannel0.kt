package de.tillhub.paymentengine.opi.communication

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocketListener
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class OPIChannel0(
    socketIp: String,
    socketPort: Int,
    private val client: OkHttpClient = OkHttpClient()
) {

    private var webSocket: okhttp3.WebSocket? = null
    private var shouldReconnect: AtomicBoolean = AtomicBoolean(false)
    private val socketUrl = "$socketIp:$socketPort"

    private var onMessage: ((String) -> Unit)? = null
    private var onError: (Throwable, String) -> Unit = {_,_ -> }

    val isConnected: Boolean
        get() = webSocket != null

    fun open() {
        shouldReconnect.set(true)

        val request = Request.Builder().url(url = socketUrl).build()
        webSocket = client.newWebSocket(request, webSocketListener)

        client.dispatcher.executorService.shutdown()
    }

    fun close() {
        shouldReconnect.set(false)

        webSocket?.close(CLOSING_CODE, "Do not need connection anymore.")
        webSocket = null
    }

    fun sendMessage(message: String, onResponse: (String) -> Unit) {
        onMessage = onResponse
        webSocket?.send(message)
    }

    fun setOnError(onError: (Throwable, String) -> Unit) {
        this.onError = onError
    }

    private fun reconnect() {
        close()
        open()
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
            Timber.d("OPI_CHANNEL", "Channel $socketUrl connected")
        }

        override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
            onMessage?.let { it(text) }
            onMessage = null
        }

        override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
            Timber.d("OPI_CHANNEL", "Channel $socketUrl closing")
        }

        override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
            Timber.d("OPI_CHANNEL", "Channel $socketUrl closed\n $code: $reason")
            if (shouldReconnect.get()) reconnect()
        }

        override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: Response?) {
            Timber.e("OPI_CHANNEL", "Channel $socketUrl failure\n $response", t)
            onError(t, response.toString())
            if (shouldReconnect.get()) reconnect()
        }
    }
    companion object {
        private const val CLOSING_CODE = 1000
    }
}