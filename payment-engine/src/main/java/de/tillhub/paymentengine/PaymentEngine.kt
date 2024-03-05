package de.tillhub.paymentengine

import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.Lifecycle
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalOperationStatus
import de.tillhub.paymentengine.helper.ManagerBuilder
import de.tillhub.paymentengine.helper.SingletonHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PaymentEngine private constructor() {

    private val configs = mutableMapOf<String, Terminal>()
    private val terminalState = MutableStateFlow<TerminalOperationStatus>(TerminalOperationStatus.Waiting)

    fun observeTerminalState(): StateFlow<TerminalOperationStatus> = terminalState

    fun putTerminalConfig(config: Terminal) {
        configs[config.name] = config
    }

    fun newPaymentManager(registry: ActivityResultRegistry): ManagerBuilder<PaymentManager> {
        return object : ManagerBuilder<PaymentManager> {
            override fun build(lifecycle: Lifecycle): PaymentManager {
                return PaymentManagerImpl(configs, terminalState, registry).also {
                    terminalState.tryEmit(TerminalOperationStatus.Waiting)
                    lifecycle.addObserver(it)
                }
            }
        }
    }

    fun newRefundManager(registry: ActivityResultRegistry): ManagerBuilder<RefundManager> {
        return object : ManagerBuilder<RefundManager> {
            override fun build(lifecycle: Lifecycle): RefundManager {
                return RefundManagerImpl(configs, terminalState, registry).also {
                    terminalState.tryEmit(TerminalOperationStatus.Waiting)
                    lifecycle.addObserver(it)
                }
            }
        }
    }

    fun newReversalManager(registry: ActivityResultRegistry): ManagerBuilder<ReversalManager> {
        return object : ManagerBuilder<ReversalManager> {
            override fun build(lifecycle: Lifecycle): ReversalManager {
                return ReversalManagerImpl(configs, terminalState, registry).also {
                    terminalState.tryEmit(TerminalOperationStatus.Waiting)
                    lifecycle.addObserver(it)
                }
            }
        }
    }

    fun newReconciliationManager(registry: ActivityResultRegistry): ManagerBuilder<ReconciliationManager> {
        return object : ManagerBuilder<ReconciliationManager> {
            override fun build(lifecycle: Lifecycle): ReconciliationManager {
                return ReconciliationManagerImpl(configs, terminalState, registry).also {
                    terminalState.tryEmit(TerminalOperationStatus.Waiting)
                    lifecycle.addObserver(it)
                }
            }
        }
    }

    companion object : SingletonHolder<PaymentEngine>(::PaymentEngine)
}