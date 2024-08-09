package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultCaller
import de.tillhub.paymentengine.analytics.PaymentAnalytics
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.helper.SingletonHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PaymentEngine private constructor() {

    private val configs = mutableMapOf<String, Terminal>()
    private val terminalState = MutableStateFlow<TerminalOperationStatus>(TerminalOperationStatus.Waiting)
    internal var paymentAnalytics: PaymentAnalytics? = null
        private set

    fun setAnalytics(paymentAnalytics: PaymentAnalytics): PaymentEngine {
        this.paymentAnalytics = paymentAnalytics
        return this
    }

    fun observeTerminalState(): StateFlow<TerminalOperationStatus> = terminalState

    fun putTerminalConfig(config: Terminal) {
        configs[config.name] = config
    }

    fun newPaymentManager(registry: ActivityResultCaller): PaymentManager {
        return PaymentManagerImpl(configs, terminalState, registry).also {
            terminalState.tryEmit(TerminalOperationStatus.Waiting)
        }
    }

    fun newRefundManager(registry: ActivityResultCaller): RefundManager {
        return RefundManagerImpl(configs, terminalState, registry).also {
            terminalState.tryEmit(TerminalOperationStatus.Waiting)
        }
    }

    fun newReversalManager(registry: ActivityResultCaller): ReversalManager {
        return ReversalManagerImpl(configs, terminalState, registry).also {
            terminalState.tryEmit(TerminalOperationStatus.Waiting)
        }
    }

    fun newReconciliationManager(registry: ActivityResultCaller): ReconciliationManager {
        return ReconciliationManagerImpl(configs, terminalState, registry).also {
            terminalState.tryEmit(TerminalOperationStatus.Waiting)
        }
    }

    companion object : SingletonHolder<PaymentEngine>(::PaymentEngine)
}