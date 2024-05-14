package de.tillhub.paymentengine.opi.communication

class OPIChannelFactory {

    fun newOPIChannel0(
        socketIp: String,
        socketPort: Int
    ): OPIChannel0 = OPIChannel0(socketIp, socketPort)

    fun newOPIChannel1(socketPort: Int): OPIChannel1 = OPIChannel1(socketPort)
}