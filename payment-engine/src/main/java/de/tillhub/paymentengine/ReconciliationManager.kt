package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalReconciliationContract
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance

/**
 * This is called to start of a terminal reconciliation,
 * it sets up the manager so the data from the operation is collected correctly.
 */
interface ReconciliationManager : CardManager {
    override fun observePaymentState(): Flow<TerminalOperationStatus.Reconciliation>
    fun startReconciliation()
    fun startReconciliation(configId: String)
    fun startReconciliation(config: Terminal)
}

internal class ReconciliationManagerImpl(
    configs: MutableMap<String, Terminal>,
    terminalState: MutableStateFlow<TerminalOperationStatus>,
    resultCaller: ActivityResultCaller,
    private val reconciliationContract: ActivityResultLauncher<Terminal> =
        resultCaller.registerForActivityResult(TerminalReconciliationContract()) { result ->
            terminalState.tryEmit(result)
        }
) : CardManagerImpl(configs, terminalState), ReconciliationManager {

    override fun observePaymentState(): Flow<TerminalOperationStatus.Reconciliation> =
        terminalState.filterIsInstance(TerminalOperationStatus.Reconciliation::class)

    override fun startReconciliation() {
        val configId = configs.values.firstOrNull()?.id.orEmpty()
        startReconciliation(configId)
    }

    override fun startReconciliation(configId: String) {
        val terminalConfig = configs[configId]
        requireNotNull(terminalConfig) { "Terminal config not found for id: $configId" }
        startReconciliation(terminalConfig)
    }

    override fun startReconciliation(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Reconciliation.Pending)
        reconciliationContract.launch(config)
    }
}