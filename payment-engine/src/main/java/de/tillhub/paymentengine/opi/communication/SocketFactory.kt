package de.tillhub.paymentengine.opi.communication

import java.net.ServerSocket
import java.net.Socket

internal class SocketFactory {
    fun createServerSocket(socketPort: Int): ServerSocket =
        ServerSocket(socketPort)

    fun createClientSocket(socketIp: String, socketPort: Int): Socket =
        Socket(socketIp, socketPort)
}