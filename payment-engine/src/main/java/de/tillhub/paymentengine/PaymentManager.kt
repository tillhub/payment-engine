package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.LifecycleOwner
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.PaymentResultContract
import de.tillhub.paymentengine.data.ISOAlphaCurrency
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal
import java.util.Random
import java.util.UUID

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
    configs: MutableMap<String, Terminal>,
    terminalState: MutableStateFlow<TerminalOperationStatus>,
    private val registry: ActivityResultRegistry,
    private val defaultConfig: Terminal = Terminal.ZVT()
) : CardManagerImpl(configs, terminalState), PaymentManager {

    private lateinit var paymentResultContract: ActivityResultLauncher<PaymentRequest>

    private val activityResultKey: String by lazy {
        "payment_" + UUID.randomUUID().toString() + "_rq#" + Random().nextInt()
    }

    override fun registerResultRegistry(owner: LifecycleOwner) {
        paymentResultContract = registry.register(
            activityResultKey, owner, PaymentResultContract()
        ) { result ->
            terminalState.tryEmit(result)
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
        terminalState.tryEmit(TerminalOperationStatus.Pending.Payment(amount, currency))
        paymentResultContract.launch(
            PaymentRequest(config, amount, currency)
        )
    }
}