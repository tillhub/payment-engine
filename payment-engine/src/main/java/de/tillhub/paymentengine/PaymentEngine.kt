package de.tillhub.paymentengine

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import de.tillhub.paymentengine.helper.ManagerBuilder
import de.tillhub.paymentengine.helper.SingletonHolder

class PaymentEngine private constructor(private val activity: ComponentActivity) {

    fun newPaymentManager(): ManagerBuilder<PaymentManager> {
        return object : ManagerBuilder<PaymentManager> {
            override fun build(lifecycle: Lifecycle): PaymentManager {
                return PaymentManagerImpl(activity.activityResultRegistry).also {
                    lifecycle.addObserver(it)
                }
            }
        }
    }

    fun newRefundManager(): ManagerBuilder<RefundManager> {
        return object : ManagerBuilder<RefundManager> {
            override fun build(lifecycle: Lifecycle): RefundManager {
                return RefundManagerImpl(activity.activityResultRegistry).also {
                    lifecycle.addObserver(it)
                }
            }
        }
    }

    fun newReversalManager(): ManagerBuilder<ReversalManager> {
        return object : ManagerBuilder<ReversalManager> {
            override fun build(lifecycle: Lifecycle): ReversalManager {
                return ReversalManagerImpl(activity.activityResultRegistry).also {
                    lifecycle.addObserver(it)
                }
            }
        }
    }

    fun newReconciliationManager(): ManagerBuilder<ReconciliationManager> {
        return object : ManagerBuilder<ReconciliationManager> {
            override fun build(lifecycle: Lifecycle): ReconciliationManager {
                return ReconciliationManagerImpl(activity.activityResultRegistry).also {
                    lifecycle.addObserver(it)
                }
            }
        }
    }

    companion object : SingletonHolder<PaymentEngine, ComponentActivity>(::PaymentEngine)
}