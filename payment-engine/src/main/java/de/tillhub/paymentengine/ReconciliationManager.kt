package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.TerminalReconciliationContract
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

/**
 * This is called to start of a terminal reconciliation,
 * it sets up the manager so the data from the operation is collected correctly.
 */
interface ReconciliationManager : CardManager {
    fun startReconciliation()
    fun startReconciliation(configName: String)
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

    override fun startReconciliation() {
        val configName = configs.values.firstOrNull()?.id.orEmpty()
        startReconciliation(configName)
    }

    override fun startReconciliation(configName: String) {
        val terminalConfig = configs.getOrDefault(configName, defaultConfig)
        startReconciliation(terminalConfig)
    }

    override fun startReconciliation(config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Pending.Reconciliation)
        try {
            reconciliationContract.launch(config)
        } catch (_: ActivityNotFoundException) {
            terminalState.tryEmit(
                TerminalOperationStatus.Error.SPOS(
                    date = Instant.now(),
                    customerReceipt = "",
                    merchantReceipt = "",
                    rawData = "",
                    data = null,
                    resultCode = ResultCodeSets.APP_NOT_FOUND_ERROR
                )
            )
        }
    }
}