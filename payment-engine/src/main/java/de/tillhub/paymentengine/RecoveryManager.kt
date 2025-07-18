package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentRecoveryContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationError
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.data.TransactionResultCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import java.time.Instant

/**
 * This is called to try to initiate recovery of last payment
 */
interface RecoveryManager : CardManager {
    override fun observePaymentState(): Flow<TerminalOperationStatus.Recovery>
    fun startRecovery()
    fun startRecovery(configId: String)
    fun startRecovery(config: Terminal)
}

internal class RecoveryManagerImpl(
    configs: MutableMap<String, Terminal>,
    terminalState: MutableStateFlow<TerminalOperationStatus>,
    resultCaller: ActivityResultCaller,
    private val recoveryContract: ActivityResultLauncher<Terminal> =
        resultCaller.registerForActivityResult(PaymentRecoveryContract()) { result ->
            terminalState.tryEmit(result)
        }
) : CardManagerImpl(configs, terminalState), RecoveryManager {

    override fun observePaymentState(): Flow<TerminalOperationStatus.Recovery> =
        terminalState.filterIsInstance(TerminalOperationStatus.Recovery::class)

    override fun startRecovery() {
        val configId = configs.values.firstOrNull()?.id.orEmpty()
        startRecovery(configId)
    }

    override fun startRecovery(configId: String) {
        val terminalConfig = configs[configId]
        requireNotNull(terminalConfig) { "Terminal config not found for id: $configId" }
        startRecovery(terminalConfig)
    }

    override fun startRecovery(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Recovery.Pending)
        try {
            recoveryContract.launch(config)
        } catch (_: UnsupportedOperationException) {
            terminalState.tryEmit(
                TerminalOperationStatus.Recovery.Error(
                    TerminalOperationError(
                        date = Instant.now(),
                        resultCode = TransactionResultCode.ACTION_NOT_SUPPORTED
                    )
                )
            )
        }
    }
}