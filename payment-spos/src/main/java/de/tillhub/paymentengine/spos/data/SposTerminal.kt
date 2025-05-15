package de.tillhub.paymentengine.spos.data

import android.content.Context
import android.content.Intent
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.spos.SPOSIntentFactory
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
class SposTerminal(
    override val id: String = DEFAULT_SPOS_ID,
    val appId: String = DEFAULT_APP_ID,
    val connected: Boolean = DEFAULT_CONNECTION,
    val currencyCode: String = DEFAULT_CURRENCY_CODE,
) : Terminal.External(id) {
    override fun connectIntent(context: Context, input: Terminal): Intent =
        SPOSIntentFactory.createConnectIntent(input as SposTerminal)

    override fun paymentIntent(context: Context, input: PaymentRequest): Intent =
        SPOSIntentFactory.createPaymentIntent(input)

    override fun refundIntent(context: Context, input: RefundRequest): Intent =
        SPOSIntentFactory.createPaymentRefundIntent(input)

    override fun reversalIntent(context: Context, input: ReversalRequest): Intent =
        SPOSIntentFactory.createPaymentReversalIntent(input)

    override fun reconciliationIntent(context: Context, input: Terminal): Intent =
        SPOSIntentFactory.createReconciliationIntent()

    override fun recoveryIntent(context: Context, input: Terminal): Intent =
        SPOSIntentFactory.createRecoveryIntent()

    override fun disconnectIntent(context: Context, input: Terminal): Intent =
        SPOSIntentFactory.createDisconnectIntent(input as SposTerminal)

    override fun ticketReprintIntent(context: Context, input: Terminal): Intent =
        SPOSIntentFactory.createTicketReprintIntent()

    override fun toString() = "SposTerminal(" +
            "id=$id, " +
            "appId=$appId, " +
            "saleConfig=$saleConfig, " +
            "currencyCode=$currencyCode" +
            ")"

    override fun equals(other: Any?) = other is SposTerminal &&
            id == other.id &&
            appId == other.appId &&
            saleConfig == other.saleConfig &&
            currencyCode == other.currencyCode

    override fun hashCode() = Objects.hash(
        id,
        appId,
        saleConfig,
        currencyCode
    )

    companion object {
        private const val DEFAULT_SPOS_ID = "Default:SPOS"
        private const val DEFAULT_APP_ID = "TESTCLIENT"
        private const val DEFAULT_CONNECTION = false
        const val DEFAULT_CURRENCY_CODE = "EUR"
        const val TYPE = "SPOS"
    }
}