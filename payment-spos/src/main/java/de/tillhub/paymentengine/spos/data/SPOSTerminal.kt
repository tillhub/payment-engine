package de.tillhub.paymentengine.spos.data

import android.content.Context
import android.content.Intent
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.ExternalTerminal
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Suppress("TooManyFunctions")
@Parcelize
class SPOSTerminal(
    override val id: String = DEFAULT_SPOS_ID,
    val appId: String = DEFAULT_APP_ID,
    val connected: Boolean = DEFAULT_CONNECTION,
    val currencyCode: String = DEFAULT_CURRENCY_CODE,
) : ExternalTerminal(id) {
    override fun connectIntent(context: Context, input: Terminal): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_CONNECT)

            putExtra(ExtraKeys.EXTRA_CONFIG, input)

            // This is needed for devices with API 34+
            `package` = context.packageName
        }

    override fun paymentIntent(context: Context, input: PaymentRequest): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_PAYMENT)

            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
            putExtra(SPOSExtraKeys.EXTRA_TIP, input.tip)
            putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, input.transactionId)

            `package` = context.packageName
        }

    override fun refundIntent(context: Context, input: RefundRequest): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_REFUND)

            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
            putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, input.transactionId)

            `package` = context.packageName
        }

    override fun reversalIntent(context: Context, input: ReversalRequest): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_REVERSAL)

            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_RECEIPT_NO, input.receiptNo)
            putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
            putExtra(SPOSExtraKeys.EXTRA_TIP, input.tip)
            putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
            putExtra(SPOSExtraKeys.EXTRA_TX_ID, input.transactionId)

            `package` = context.packageName
        }

    override fun reconciliationIntent(context: Context, input: Terminal): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_RECONCILIATION)

            putExtra(ExtraKeys.EXTRA_CONFIG, input)

            `package` = context.packageName
        }

    override fun recoveryIntent(context: Context, input: Terminal): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_RECOVERY)

            putExtra(ExtraKeys.EXTRA_CONFIG, input)

            `package` = context.packageName
        }

    override fun disconnectIntent(context: Context, input: Terminal): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_DISCONNECT)

            putExtra(ExtraKeys.EXTRA_CONFIG, input)

            `package` = context.packageName
        }

    override fun ticketReprintIntent(context: Context, input: Terminal): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_REPRINT)

            putExtra(ExtraKeys.EXTRA_CONFIG, input)

            `package` = context.packageName
        }

    override fun toString() = "SposTerminal(" +
            "id=$id, " +
            "appId=$appId, " +
            "saleConfig=$saleConfig, " +
            "connected=$connected, " +
            "currencyCode=$currencyCode" +
            ")"

    override fun equals(other: Any?) = other is SPOSTerminal &&
            id == other.id &&
            appId == other.appId &&
            saleConfig == other.saleConfig &&
            connected == other.connected &&
            currencyCode == other.currencyCode

    override fun hashCode() = Objects.hash(
        id,
        appId,
        saleConfig,
        connected,
        currencyCode
    )

    companion object {
        private const val DEFAULT_SPOS_ID = "Default:SPOS"
        private const val DEFAULT_APP_ID = "TESTCLIENT"
        private const val DEFAULT_CONNECTION = false
        const val DEFAULT_CURRENCY_CODE = "EUR"
        const val TYPE = "SPOS"

        private const val INTENT_ACTION_SPOS = "de.tillhub.paymentengine.spos.ACTION_SPOS"
    }
}