package de.tillhub.paymentengine.testing

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
import java.util.Objects

@Parcelize
class TestExternalTerminal(
    override val id: String,
    override val saleConfig: CardSaleConfig = CardSaleConfig(),
    @IgnoredOnParcel
    override val contract: TerminalContract = TestExternalTerminalContract(),
) : Terminal {

    override fun toString() = "Terminal.External(" +
            "id=$id, " +
            "saleConfig=$saleConfig" +
            ")"

    override fun equals(other: Any?) = other is TestExternalTerminal &&
            id == other.id &&
            saleConfig == other.saleConfig

    override fun hashCode() = Objects.hash(
        id,
        saleConfig,
    )
}

class TestExternalTerminalContract : TerminalContract {
    override fun connectIntent(context: Context, terminal: Terminal): Intent =
        Intent("CONNECT")
    override fun paymentIntent(context: Context, input: PaymentRequest): Intent =
        Intent("PAYMENT")
    override fun refundIntent(context: Context, input: RefundRequest): Intent =
        Intent("REFUND")
    override fun reversalIntent(context: Context, input: ReversalRequest): Intent =
        Intent("REVERSAL")
    override fun reconciliationIntent(context: Context, terminal: Terminal): Intent =
        Intent("RECONCILIATION")
    override fun recoveryIntent(context: Context, terminal: Terminal): Intent =
        Intent("RECOVERY")
    override fun disconnectIntent(context: Context, terminal: Terminal): Intent =
        Intent("DISCONNECT")
    override fun ticketReprintIntent(context: Context, terminal: Terminal): Intent =
        Intent("REPRINT")
}