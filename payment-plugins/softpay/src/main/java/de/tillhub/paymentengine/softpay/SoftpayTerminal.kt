package de.tillhub.paymentengine.softpay

import android.content.Context
import android.content.Intent
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalContract
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class SoftpayTerminal internal constructor(
    override val id: String = DEFAULT_SOFTPAY_ID,
    override val saleConfig: CardSaleConfig = CardSaleConfig(),
    @IgnoredOnParcel
    override val contract: TerminalContract = SoftpayTerminalContract(),
) : Terminal {

    companion object {
        private const val DEFAULT_SOFTPAY_ID = "Default:SOFTPAY"
    }
}

internal class SoftpayTerminalContract : TerminalContract {

    override fun connectIntent(context: Context, terminal: Terminal): Intent {
        throw UnsupportedOperationException("Connect is not supported by this terminal")
    }
    override fun paymentIntent(context: Context, input: PaymentRequest): Intent {
        throw UnsupportedOperationException("Payment is not supported by this terminal")
    }
    override fun refundIntent(context: Context, input: RefundRequest): Intent {
        throw UnsupportedOperationException("Refund is not supported by this terminal")
    }
    override fun reversalIntent(context: Context, input: ReversalRequest): Intent {
        throw UnsupportedOperationException("Payment reversal is not supported by this terminal")
    }
    override fun reconciliationIntent(context: Context, terminal: Terminal): Intent {
        throw UnsupportedOperationException("Reconciliation is not supported by this terminal")
    }
    override fun ticketReprintIntent(context: Context, terminal: Terminal): Intent {
        throw UnsupportedOperationException("Ticket reprint is not supported by this terminal")
    }
    override fun recoveryIntent(context: Context, terminal: Terminal): Intent {
        throw UnsupportedOperationException("Payment recovery is not supported by this terminal")
    }
    override fun disconnectIntent(context: Context, terminal: Terminal): Intent {
        throw UnsupportedOperationException("Disconnect is not supported by this terminal")
    }
}