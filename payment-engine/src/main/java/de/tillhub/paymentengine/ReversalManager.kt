package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentReversalContract
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import java.math.BigDecimal

/**
 * This is called to start of a card payment reversal,
 * it sets up the manager so the data from the transaction is collected correctly.
 */
interface ReversalManager : CardManager {
    override fun observePaymentState(): Flow<TerminalOperationStatus.Reversal>
    fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal = BigDecimal.ZERO,
        currency: ISOAlphaCurrency,
        receiptNo: String
    )

    @Suppress("LongParameterList")
    fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal = BigDecimal.ZERO,
        currency: ISOAlphaCurrency,
        receiptNo: String,
        configId: String,
    )

    @Suppress("LongParameterList")
    fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal = BigDecimal.ZERO,
        currency: ISOAlphaCurrency,
        receiptNo: String,
        config: Terminal,
    )
}

internal class ReversalManagerImpl(
    configs: MutableMap<String, Terminal>,
    terminalState: MutableStateFlow<TerminalOperationStatus>,
    resultCaller: ActivityResultCaller,
    private val reversalContract: ActivityResultLauncher<ReversalRequest> =
        resultCaller.registerForActivityResult(PaymentReversalContract()) { result ->
            terminalState.tryEmit(result)
        }
) : CardManagerImpl(configs, terminalState), ReversalManager {

    override fun observePaymentState(): Flow<TerminalOperationStatus.Reversal> =
        terminalState.filterIsInstance(TerminalOperationStatus.Reversal::class)

    override fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency,
        receiptNo: String
    ) {
        val configId = configs.values.firstOrNull()?.id.orEmpty()
        startReversalTransaction(
            transactionId = transactionId,
            amount = amount,
            tip = tip,
            currency = currency,
            configId = configId,
            receiptNo = receiptNo
        )
    }

    override fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency,
        receiptNo: String,
        configId: String
    ) {
        val terminalConfig = configs.getOrDefault(configId, defaultConfig)
        startReversalTransaction(
            transactionId = transactionId,
            amount = amount,
            tip = tip,
            currency = currency,
            receiptNo = receiptNo,
            config = terminalConfig
        )
    }

    override fun startReversalTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency,
        receiptNo: String,
        config: Terminal
    ) {
        terminalState.tryEmit(TerminalOperationStatus.Reversal.Pending(receiptNo))
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