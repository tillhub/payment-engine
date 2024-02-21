package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.LifecycleOwner
import de.tillhub.paymentengine.contract.PaymentReversalContract
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus

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
    private val registry: ActivityResultRegistry,
    private val defaultConfig: Terminal = Terminal.ZVT()
) : CardManagerImpl(), ReversalManager {

    private lateinit var reversalContract: ActivityResultLauncher<ReversalRequest>

    override fun registerResultRegistry(owner: LifecycleOwner) {
        reversalContract = registry.register(
            PaymentReversalContract.REGISTER_KEY, owner, PaymentReversalContract()
        ) { result ->
            transactionState.tryEmit(result)
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
        transactionState.tryEmit(TerminalOperationStatus.Pending.Reversal(receiptNo))
        reversalContract.launch(
            ReversalRequest(config, receiptNo)
        )
    }
}