package de.tillhub.paymentengine.demo.storage

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow

interface DeviceStorage {
    val terminalData: Flow<TerminalData>
    suspend fun storeTerminalData(terminalData: TerminalData)
}

class DeviceStorageImpl(
    private val terminalDataDs: DataStore<TerminalData>,
) : DeviceStorage {

    override val terminalData: Flow<TerminalData>
        get() = terminalDataDs.data

    override suspend fun storeTerminalData(terminalData: TerminalData) {
        terminalDataDs.updateData {
            terminalData
        }
    }
}
