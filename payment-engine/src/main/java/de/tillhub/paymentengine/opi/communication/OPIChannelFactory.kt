package de.tillhub.paymentengine.opi.communication

import okhttp3.OkHttpClient

class OPIChannelFactory {

    fun newOPIChannel0(
        socketIp: String,
        socketPort: Int,
        client: OkHttpClient = OkHttpClient()
    ): OPIChannel0 = OPIChannel0(socketIp, socketPort, client)

    fun newOPIChannel1(socketPort: Int): OPIChannel1 = OPIChannel1(socketPort)
}