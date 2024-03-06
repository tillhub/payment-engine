package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentReversalContract
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * This is called to start of a card payment reversal,
 * it sets up the manager so the data from the transaction is collected correctly.
 */
interface ReversalManager : CardManager {
    fun startReversalTransaction(receiptNo: String)
    fun startReversalTransaction(receiptNo: String, configName: String)
    fun startReversalTransaction(receiptNo: String, config: Terminal)
}

class ReversalManagerImpl(
    configs: MutableMap<String, Terminal>,
    transactionState: MutableStateFlow<TerminalOperationStatus>,
    resultCaller: ActivityResultCaller
) : CardManagerImpl(configs, transactionState), ReversalManager {

    private val reversalContract: ActivityResultLauncher<ReversalRequest> =
        resultCaller.registerForActivityResult(PaymentReversalContract()) { result ->
            terminalState.tryEmit(result)
        }

    override fun startReversalTransaction(receiptNo: String) {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startReversalTransaction(receiptNo, configName)
    }

    override fun startReversalTransaction(receiptNo: String, configName: String) {
        val terminalConfig = configs.getOrDefault(configName, defaultConfig)
        startReversalTransaction(receiptNo, terminalConfig)
    }

    override fun startReversalTransaction(receiptNo: String, config: Terminal) {
        terminalState.tryEmit(TerminalOperationStatus.Pending.Reversal(receiptNo))
        reversalContract.launch(
            ReversalRequest(config, receiptNo)
        )
    }
}