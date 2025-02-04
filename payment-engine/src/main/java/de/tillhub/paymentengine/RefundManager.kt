package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentRefundContract
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal
import java.time.Instant

/**
 * This is called to start of a partial card payment refund,
 * it sets up the manager so the data from the transaction is collected correctly.
 */
interface RefundManager : CardManager {
    fun startRefundTransaction(
        transactionId: String,
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    )

    fun startRefundTransaction(
        transactionId: String,
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        configId: String
    )

    fun startRefundTransaction(
        transactionId: String,
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        config: Terminal
    )
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

    override fun startRefundTransaction(
        transactionId: String,
        amount: BigDecimal,
        currency: ISOAlphaCurrency
    ) {
        val configId = configs.values.firstOrNull()?.id.orEmpty()
        startRefundTransaction(transactionId, amount, currency, configId)
    }

    override fun startRefundTransaction(
        transactionId: String,
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        configId: String
    ) {
        val terminalConfig = configs.getOrDefault(configId, defaultConfig)
        startRefundTransaction(transactionId, amount, currency, terminalConfig)
    }

    override fun startRefundTransaction(
        transactionId: String,
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        config: Terminal
    ) {
        terminalState.tryEmit(TerminalOperationStatus.Pending.Refund(amount, currency))
        try {
            refundContract.launch(
                RefundRequest(
                    config = config,
                    transactionId = transactionId,
                    amount = amount,
                    currency = currency
                )
            )
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