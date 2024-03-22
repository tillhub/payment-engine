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
    private val client: OkHttpClient = OkHttpClient(),
    private val onMessage: (String) -> Unit,
    private val onError: (Throwable, String) -> Unit,
) : OPIChannel {

    private var webSocket: okhttp3.WebSocket? = null
    private var shouldReconnect: AtomicBoolean = AtomicBoolean(false)
    private val socketUrl = "$socketIp:$socketPort"

    override val isConnected: Boolean
        get() = webSocket != null

    override suspend fun open() {
        openInternal()
    }

    override fun close() {
        shouldReconnect.set(false)

        webSocket?.close(CLOSING_CODE, "Do not need connection anymore.")
        webSocket = null
    }

    override fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    private fun openInternal() {
        shouldReconnect.set(true)

        val request = Request.Builder().url(url = socketUrl).build()
        webSocket = client.newWebSocket(request, webSocketListener)

        client.dispatcher.executorService.shutdown()
    }

    private fun reconnect() {
        close()
        openInternal()
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
            Timber.d("OPI_CHANNEL", "Channel $socketUrl connected")
        }

        override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
            onMessage(text)
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