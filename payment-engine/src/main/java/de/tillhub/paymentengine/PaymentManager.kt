package de.tillhub.paymentengine

import android.content.ActivityNotFoundException
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.PaymentResultContract
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.ResultCodeSets
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal
import java.time.Instant

/**
 * This is called to start of a card payment transaction,
 * it sets up the manager so the data from the transaction is collected correctly.
 */
interface PaymentManager : CardManager {
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
        configName: String
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

    override fun startPaymentTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency
    ) {
        val configName = configs.values.firstOrNull()?.id.orEmpty()
        startPaymentTransaction(transactionId, amount, tip, currency, configName)
    }

    override fun startPaymentTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency,
        configName: String
    ) {
        val terminalConfig = configs.getOrDefault(configName, defaultConfig)
        startPaymentTransaction(transactionId, amount, tip, currency, terminalConfig)
    }

    override fun startPaymentTransaction(
        transactionId: String,
        amount: BigDecimal,
        tip: BigDecimal,
        currency: ISOAlphaCurrency,
        config: Terminal
    ) {
        terminalState.tryEmit(TerminalOperationStatus.Pending.Payment(amount, currency))
        try {
            paymentResultContract.launch(
                PaymentRequest(config, transactionId, amount, tip, currency)
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