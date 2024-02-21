package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.LifecycleOwner
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.PaymentResultContract
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import java.math.BigDecimal

/**
 * This is called to start of a card payment transaction,
 * it sets up the manager so the data from the transaction is collected correctly.
 */
interface PaymentManager : CardManager {
    fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency)
    fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency, configName: String)
    fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency, config: Terminal)
}

class PaymentManagerImpl(
    private val registry: ActivityResultRegistry,
    private val defaultConfig: Terminal = Terminal.ZVT()
) : CardManagerImpl(), PaymentManager {

    private lateinit var paymentResultContract: ActivityResultLauncher<PaymentRequest>

    override fun registerResultRegistry(owner: LifecycleOwner) {
        paymentResultContract = registry.register(
            PaymentResultContract.REGISTER_KEY, owner, PaymentResultContract()
        ) { result ->
            transactionState.tryEmit(result)
        }
    }

    override fun startPaymentTransaction(amount: BigDecimal, currency: ISOAlphaCurrency) {
        val configName = configs.values.firstOrNull()?.name.orEmpty()
        startPaymentTransaction(amount, currency, configName)
    }

    override fun startPaymentTransaction(
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        configName: String
    ) {
        val terminalConfig = configs.getOrDefault(configName, defaultConfig)
        startPaymentTransaction(amount, currency, terminalConfig)
    }

    override fun startPaymentTransaction(
        amount: BigDecimal,
        currency: ISOAlphaCurrency,
        config: Terminal
    ) {
        transactionState.tryEmit(TerminalOperationStatus.Pending.Payment(amount, currency))
        paymentResultContract.launch(
            PaymentRequest(config, amount, currency)
        )
    }
}