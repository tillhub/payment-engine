package de.tillhub.paymentengine

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import de.tillhub.paymentengine.ui.CardPaymentActivity
import de.tillhub.paymentengine.ui.CardPaymentPartialRefundActivity
import de.tillhub.paymentengine.ui.CardPaymentReversalActivity
import de.tillhub.paymentengine.ui.TerminalReconciliationActivity

abstract class PaymentTerminalConnection {
    internal abstract fun disconnect()
}

class LavegoConnection(
    private var componentActivity: ComponentActivity?,
    private val appContext: Context,
) : PaymentTerminalConnection() {

    override fun disconnect() {
        componentActivity = null
    }

    fun startPaymentTransaction() {
        componentActivity?.startActivity(paymentIntent(appContext))
    }

    fun startPaymentReversal() {
        componentActivity?.startActivity(reversalIntent(appContext))
    }

    fun startPartialRefund() {
        componentActivity?.startActivity(partialRefundIntent(appContext))
    }

    fun startReconciliation() {
        componentActivity?.startActivity(reconciliationIntent(appContext))
    }

    companion object {
        private fun paymentIntent(context: Context): Intent =
            Intent(context, CardPaymentActivity::class.java)

        private fun reversalIntent(context: Context): Intent =
            Intent(context, CardPaymentReversalActivity::class.java)

        private fun partialRefundIntent(context: Context): Intent =
            Intent(context, CardPaymentPartialRefundActivity::class.java)

        private fun reconciliationIntent(context: Context): Intent =
            Intent(context, TerminalReconciliationActivity::class.java)
    }
}