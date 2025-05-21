package de.tillhub.paymentengine.testing

import android.content.Context
import android.content.Intent
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.Terminal
import kotlinx.parcelize.Parcelize

@Parcelize
class TestExternalTerminal(
    override val id: String,
) : Terminal.External(id) {
    override fun connectIntent(context: Context, input: Terminal): Intent = Intent("CONNECT")
    override fun paymentIntent(context: Context, input: PaymentRequest): Intent = Intent("PAYMENT")
    override fun refundIntent(context: Context, input: RefundRequest): Intent = Intent("REFUND")
    override fun reversalIntent(context: Context, input: ReversalRequest): Intent = Intent("REVERSAL")
    override fun reconciliationIntent(context: Context, input: Terminal): Intent = Intent("RECONCILIATION")
    override fun recoveryIntent(context: Context, input: Terminal): Intent = Intent("RECOVERY")
    override fun disconnectIntent(context: Context, input: Terminal): Intent = Intent("DISCONNECT")
    override fun ticketReprintIntent(context: Context, input: Terminal): Intent = Intent("REPRINT")
}