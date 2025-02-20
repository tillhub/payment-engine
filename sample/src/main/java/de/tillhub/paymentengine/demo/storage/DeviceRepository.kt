package de.tillhub.paymentengine.demo.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface DeviceRepository {
    val terminal: Flow<TerminalData>
    fun createTerminalData()

    fun updateTerminal(
        ipAddress: String,
        port1: String,
        port2: String,
    )
}

class DeviceRepositoryImpl(
    private val storage: DeviceStorage,
    private val scope: CoroutineScope,
) : DeviceRepository {

    override val terminal: Flow<TerminalData>
        get() = storage.terminalData

    override fun createTerminalData() {
        scope.launch {
            storage.storeTerminalData(TerminalData())
        }
    }

    override fun updateTerminal(ipAddress: String, port1: String, port2: String) {
        scope.launch {
            storage.storeTerminalData(TerminalData(ipAddress, port1, port2))
        }
    }
}
