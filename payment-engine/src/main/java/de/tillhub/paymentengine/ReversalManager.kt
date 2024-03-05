package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.LifecycleOwner
import de.tillhub.paymentengine.contract.PaymentReversalContract
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Random
import java.util.UUID

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
    private val registry: ActivityResultRegistry,
    private val defaultConfig: Terminal = Terminal.ZVT()
) : CardManagerImpl(configs, transactionState), ReversalManager {

    private lateinit var reversalContract: ActivityResultLauncher<ReversalRequest>

    private val activityResultKey: String by lazy {
        "reversal_" + UUID.randomUUID().toString() + "_rq#" + Random().nextInt()
    }

    override fun registerResultRegistry(owner: LifecycleOwner) {
        reversalContract = registry.register(
            activityResultKey, owner, PaymentReversalContract()
        ) { result ->
            terminalState.tryEmit(result)
        }
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