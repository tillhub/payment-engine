package de.tillhub.paymentengine.opi.communication

interface OPIChannel {
    val isConnected: Boolean

    suspend fun open()
    fun close()
    fun sendMessage(message: String)
}