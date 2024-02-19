package de.tillhub.paymentengine

import androidx.activity.ComponentActivity
import de.tillhub.paymentengine.helper.SingletonHolder

class PaymentEngine private constructor(activity: ComponentActivity) {

    val cardPaymentManager: CardPaymentManager by lazy {
        CardPaymentManagerImpl(activity.activityResultRegistry)
    }
    init {
        activity.lifecycle.addObserver(cardPaymentManager)
    }

    companion object : SingletonHolder<PaymentEngine, ComponentActivity>(::PaymentEngine)
}