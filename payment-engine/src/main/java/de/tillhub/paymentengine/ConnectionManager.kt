package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalConnectContract
import de.tillhub.paymentengine.contract.TerminalDisconnectContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * This is called to start of S-POS terminal connect and disconnect,
 * it sets up the manager so the connection to the terminal is viable.
 */
interface ConnectionManager : CardManager {
    fun startSPOSConnect()
    fun startSPOSConnect(configName: String)
    fun startSPOSConnect(config: Terminal)

    fun startSPOSDisconnect()
    fun startSPOSDisconnect(configName: String)
    fun startSPOSDisconnect(config: Terminal)
}

internal class ConnectionManagerImpl(
    configs: MutableMap<String, Terminal>,
    terminalState: MutableStateFlow<TerminalOperationStatus>,
    resultCaller: ActivityResultCaller
) : CardManagerImpl(configs, terminalState), ConnectionManager {

    private val connectContract: ActivityResultLauncher<Terminal> =
        resultCaller.registerForActivityResult(TerminalConnectContract()) { result ->
            terminalState.tryEmit(result)
        }

    private val disconnectContract: ActivityResultLauncher<Terminal> =
        resultCaller.registerForActivityResult(TerminalDisconnectContract()) { result ->
            terminalState.tryEmit(result)
        }

    override fun startSPOSConnect() {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startSPOSConnect(configName)
    }

    override fun startSPOSConnect(configName: String) {
        val terminalConfig = configs.getOrDefault(configName, defaultConfig)
        startSPOSConnect(terminalConfig)
    }

    override fun startSPOSConnect(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Pending.Connecting)
        connectContract.launch(config)
    }

    override fun startSPOSDisconnect() {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startSPOSDisconnect(configName)
    }

    override fun startSPOSDisconnect(configName: String) {
        val terminalConfig = configs.getOrDefault(configName, defaultConfig)
        startSPOSDisconnect(terminalConfig)
    }

    override fun startSPOSDisconnect(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Pending.Disconnecting)
        disconnectContract.launch(config)
    }
}