package de.tillhub.paymentengine.spos.data

import android.content.Context
import android.content.Intent
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalContract
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Objects

/**
 * Represents a Software Point of Sale (SPOS) terminal.
 *
 * @property id The unique identifier of the SPOS terminal.
 * @property saleConfig Configuration for card sales.
 * @property appId The application ID for the SPOS terminal.
 * @property connected Indicates whether the terminal is currently in a connected state.
 * @property currencyCode The default currency code for transactions (e.g., "EUR").
 */
@Parcelize
class SposTerminal private constructor(
    override val id: String = DEFAULT_SPOS_ID,
    override val saleConfig: CardSaleConfig = CardSaleConfig(),
    val appId: String = DEFAULT_APP_ID,
    val connected: Boolean = DEFAULT_CONNECTION,
    val currencyCode: String = DEFAULT_CURRENCY_CODE,
) : Terminal {
    @IgnoredOnParcel
    override val contract: TerminalContract = SposTerminalContract

    override fun toString() = "SposTerminal(" +
            "id=$id, " +
            "appId=$appId, " +
            "saleConfig=$saleConfig, " +
            "connected=$connected, " +
            "currencyCode=$currencyCode" +
            ")"

    override fun equals(other: Any?) = other is SposTerminal &&
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

        fun create(
            id: String = DEFAULT_SPOS_ID,
            saleConfig: CardSaleConfig = CardSaleConfig(),
            appId: String = DEFAULT_APP_ID,
            connected: Boolean = DEFAULT_CONNECTION,
            currencyCode: String = DEFAULT_CURRENCY_CODE,
        ): SposTerminal = SposTerminal(
            id = id,
            saleConfig = saleConfig,
            appId = appId,
            connected = connected,
            currencyCode = currencyCode
        )
    }
}

/**
 * Contract for interacting with an SPOS terminal.
 * This object defines the Intents used to communicate various actions
 * (connect, payment, refund, etc.) to the SPOS terminal application.
 *
 * Note: `package = context.packageName` is set for compatibility with Android API 34+.
 */
@Suppress("TooManyFunctions")
internal object SposTerminalContract : TerminalContract {
    override fun connectIntent(context: Context, terminal: Terminal): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_CONNECT)

            putExtra(ExtraKeys.EXTRA_CONFIG, terminal)

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

    override fun reconciliationIntent(context: Context, terminal: Terminal): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_RECONCILIATION)

            putExtra(ExtraKeys.EXTRA_CONFIG, terminal)

            `package` = context.packageName
        }

    override fun recoveryIntent(context: Context, terminal: Terminal): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_RECOVERY)

            putExtra(ExtraKeys.EXTRA_CONFIG, terminal)

            `package` = context.packageName
        }

    override fun disconnectIntent(context: Context, terminal: Terminal): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_DISCONNECT)

            putExtra(ExtraKeys.EXTRA_CONFIG, terminal)

            `package` = context.packageName
        }

    override fun ticketReprintIntent(context: Context, terminal: Terminal): Intent =
        Intent(INTENT_ACTION_SPOS).apply {
            putExtra(SPOSExtraKeys.EXTRA_ACTION, SPOSExtraKeys.ACTION_REPRINT)

            putExtra(ExtraKeys.EXTRA_CONFIG, terminal)

            `package` = context.packageName
        }

    private const val INTENT_ACTION_SPOS = "de.tillhub.paymentengine.spos.ACTION_SPOS"
}