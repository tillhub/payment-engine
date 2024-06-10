package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentRefundContract
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal

/**
 * This is called to start of a partial card payment refund,
 * it sets up the manager so the data from the transaction is collected correctly.
 */
interface RefundManager : CardManager {
    fun startRefundTransaction(amount: BigDecimal, currency: ISOAlphaCurrency)
    fun startRefundTransaction(amount: BigDecimal, currency: ISOAlphaCurrency, configName: String)
    fun startRefundTransaction(amount: BigDecimal, currency: ISOAlphaCurrency, config: Terminal)
}

internal class RefundManagerImpl(
    configs: MutableMap<String, Terminal>,
    terminalState: MutableStateFlow<TerminalOperationStatus>,
    resultCaller: ActivityResultCaller
) : CardManagerImpl(configs, terminalState), RefundManager {

    private val refundContract: ActivityResultLauncher<RefundRequest> =
        resultCaller.registerForActivityResult(PaymentRefundContract()) { result ->
            terminalState.tryEmit(result)
        }

    override fun startRefundTransaction(amount: BigDecimal, currency: ISOAlphaCurrency) {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startRefundTransaction(amount, currency, configName)
    }

    override fun startRefundTransaction(
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        configName: String
    ) {
        val terminalConfig = configs.getOrDefault(configName, defaultConfig)
        startRefundTransaction(amount, currency, terminalConfig)
    }

    override fun startRefundTransaction(
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        config: Terminal
    ) {
        terminalState.tryEmit(TerminalOperationStatus.Pending.Refund(amount, currency))
        refundContract.launch(
            RefundRequest(config, amount, currency)
        )
    }
}