package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.PaymentResultContract
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import java.math.BigDecimal

/**
 * This is called to start of a card payment transaction,
 * it sets up the manager so the data from the transaction is collected correctly.
 */
interface PaymentManager : CardManager {
    override fun observePaymentState(): Flow<TerminalOperationStatus.Payment>

    fun startPaymentTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal = BigDecimal.ZERO,
        currency: ISOAlphaCurrency
    )

    fun startPaymentTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal = BigDecimal.ZERO,
        currency: ISOAlphaCurrency,
        configId: String
    )

    fun startPaymentTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal = BigDecimal.ZERO,
        currency: ISOAlphaCurrency,
        config: Terminal
    )
}

internal class PaymentManagerImpl(
    configs: MutableMap<String, Terminal>,
    terminalState: MutableStateFlow<TerminalOperationStatus>,
    resultCaller: ActivityResultCaller,
    private val paymentResultContract: ActivityResultLauncher<PaymentRequest> =
        resultCaller.registerForActivityResult(PaymentResultContract()) { result ->
            terminalState.tryEmit(result)
        }
) : CardManagerImpl(configs, terminalState), PaymentManager {

    override fun observePaymentState(): Flow<TerminalOperationStatus.Payment> =
        terminalState.filterIsInstance(TerminalOperationStatus.Payment::class)

    override fun startPaymentTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency
    ) {
        val configId = configs.values.firstOrNull()?.id.orEmpty()
        startPaymentTransaction(transactionId, amount, tip, currency, configId)
    }

    override fun startPaymentTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency,
        configId: String
    ) {
        val terminalConfig = configs.getOrDefault(configId, defaultConfig)
        startPaymentTransaction(transactionId, amount, tip, currency, terminalConfig)
    }

    override fun startPaymentTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency,
        config: Terminal
    ) {
        terminalState.tryEmit(TerminalOperationStatus.Payment.Pending(amount, currency))
        paymentResultContract.launch(
            PaymentRequest(config, transactionId, amount, tip, currency)
        )
    }
}