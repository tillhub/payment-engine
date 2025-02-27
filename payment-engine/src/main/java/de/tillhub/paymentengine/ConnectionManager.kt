package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalConnectContract
import de.tillhub.paymentengine.contract.TerminalDisconnectContract
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationError
import de.tillhub.paymentengine.data.TerminalOperationStatus
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
        val terminalConfig = configs.getOrDefault(configId, defaultConfig)
        startConnect(terminalConfig)
    }

    override fun startConnect(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
        try {
            connectContract.launch(config)
        } catch (_: ActivityNotFoundException) {
            terminalState.tryEmit(
                TerminalOperationStatus.Login.Error(
                    TerminalOperationError(
                        date = Instant.now(),
                        resultCode = ResultCodeSets.APP_NOT_FOUND_ERROR
                    )
                )
            )
        }
    }

    override fun startSPOSDisconnect() {
        val configId = configs.values.firstOrNull()?.id.orEmpty()
        startSPOSDisconnect(configId)
    }

    override fun startSPOSDisconnect(configId: String) {
        val terminalConfig = configs.getOrDefault(configId, defaultConfig)
        startSPOSDisconnect(terminalConfig)
    }

    override fun startSPOSDisconnect(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Login.Pending)
        try {
            disconnectContract.launch(config)
        } catch (_: ActivityNotFoundException) {
            terminalState.tryEmit(
                TerminalOperationStatus.Login.Error(
                    TerminalOperationError(
                        date = Instant.now(),
                        resultCode = ResultCodeSets.APP_NOT_FOUND_ERROR
                    )
                )
            )
        }
    }
}