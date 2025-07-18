package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalConnectContract
import de.tillhub.paymentengine.contract.TerminalDisconnectContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TransactionResultCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import java.time.Instant

/**
 * This is called to as initial process to connect to terminal and fetch it's data,
 * it sets up the manager so the connection to the terminal is viable.
 */
interface ConnectionManager : CardManager {
    override fun observePaymentState(): Flow<TerminalOperationStatus.Login>
    fun startConnect()
    fun startConnect(configId: String)
    fun startConnect(config: Terminal)

    fun startSPOSDisconnect()
    fun startSPOSDisconnect(configId: String)
    fun startSPOSDisconnect(config: Terminal)
}

internal class ConnectionManagerImpl(
    configs: MutableMap<String, Terminal>,
    terminalState: MutableStateFlow<TerminalOperationStatus>,
    resultCaller: ActivityResultCaller,
    private val connectContract: ActivityResultLauncher<Terminal> =
        resultCaller.registerForActivityResult(TerminalConnectContract()) { result ->
            terminalState.tryEmit(result)
        },
    private val disconnectContract: ActivityResultLauncher<Terminal> =
        resultCaller.registerForActivityResult(TerminalDisconnectContract()) { result ->
            terminalState.tryEmit(result)
        }
) : CardManagerImpl(configs, terminalState), ConnectionManager {

    override fun observePaymentState(): Flow<TerminalOperationStatus.Login> =
        terminalState.filterIsInstance(TerminalOperationStatus.Login::class)

    override fun startConnect() {
        val configId = configs.values.firstOrNull()?.id.orEmpty()
        startConnect(configId)
    }

    override fun startConnect(configId: String) {
        val terminalConfig = configs[configId]
        requireNotNull(terminalConfig) { "Terminal config not found for id: $configId" }
        startConnect(terminalConfig)
    }

    override fun startConnect(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
        connectContract.launch(config)
    }

    override fun startSPOSDisconnect() {
        val configId = configs.values.firstOrNull()?.id.orEmpty()
        startSPOSDisconnect(configId)
    }

    override fun startSPOSDisconnect(configId: String) {
        val terminalConfig = configs[configId]
        requireNotNull(terminalConfig) { "Terminal config not found for id: $configId" }
        startSPOSDisconnect(terminalConfig)
    }

    override fun startSPOSDisconnect(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
        try {
            disconnectContract.launch(config)
        } catch (_: UnsupportedOperationException) {
            terminalState.tryEmit(
                TerminalOperationStatus.Login.Error(
                    date = Instant.now(),
                    rawData = "",
                    resultCode = TransactionResultCode.ACTION_NOT_SUPPORTED
                )
            )
        }
    }
}