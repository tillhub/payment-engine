package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentReversalContract
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal

/**
 * This is called to start of a card payment reversal,
 * it sets up the manager so the data from the transaction is collected correctly.
 */
interface ReversalManager : CardManager {
    fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal = BigDecimal.ZERO,
        currency: ISOAlphaCurrency,
        receiptNo: String
    )

    fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal = BigDecimal.ZERO,
        currency: ISOAlphaCurrency,
        configName: String,
        receiptNo: String,
    )

    fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal = BigDecimal.ZERO,
        currency: ISOAlphaCurrency,
        config: Terminal,
        receiptNo: String
    )
}

internal class ReversalManagerImpl(
    configs: MutableMap<String, Terminal>,
    transactionState: MutableStateFlow<TerminalOperationStatus>,
    resultCaller: ActivityResultCaller
) : CardManagerImpl(configs, transactionState), ReversalManager {

    private val reversalContract: ActivityResultLauncher<ReversalRequest> =
        resultCaller.registerForActivityResult(PaymentReversalContract()) { result ->
            terminalState.tryEmit(result)
        }

    override fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency,
        receiptNo: String
    ) {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startReversalTransaction(
            transactionId = transactionId,
            amount = amount,
            tip = tip,
            currency = currency,
            configName = receiptNo,
            receiptNo = configName
        )
    }

    override fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency,
        configName: String,
        receiptNo: String
    ) {
        val terminalConfig = configs.getOrDefault(configName, defaultConfig)
        startReversalTransaction(
            transactionId = transactionId,
            amount = amount,
            tip = tip,
            currency = currency,
            config = terminalConfig,
            receiptNo = configName
        )
    }

    override fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency,
        config: Terminal,
        receiptNo: String
    ) {
        terminalState.tryEmit(TerminalOperationStatus.Pending.Reversal(receiptNo))
        reversalContract.launch(
            ReversalRequest(
                transactionId = transactionId,
                amount = amount,
                tip = tip,
                currency = currency,
                config = config,
                receiptNo = receiptNo
            )
        )
    }
}