package de.tillhub.paymentengine.data

import android.content.Context
import android.content.Intent
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest

abstract class ExternalTerminal(
    override val id: String = DEFAULT_EXTERNAL_ID,
    override val saleConfig: CardSaleConfig = CardSaleConfig(),
) : Terminal {
    abstract fun connectIntent(context: Context, input: Terminal): Intent
    abstract fun paymentIntent(context: Context, input: PaymentRequest): Intent
    abstract fun refundIntent(context: Context, input: RefundRequest): Intent
    abstract fun reversalIntent(context: Context, input: ReversalRequest): Intent
    abstract fun reconciliationIntent(context: Context, input: Terminal): Intent
    abstract fun ticketReprintIntent(context: Context, input: Terminal): Intent
    abstract fun recoveryIntent(context: Context, input: Terminal): Intent
    abstract fun disconnectIntent(context: Context, input: Terminal): Intent

    companion object {
        private const val DEFAULT_EXTERNAL_ID = "Default:External"
    }
}