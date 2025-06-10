package de.tillhub.paymentengine.opi.data

import android.content.Context
import android.content.Intent
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalContract
import de.tillhub.paymentengine.opi.ui.OPILoginActivity
import de.tillhub.paymentengine.opi.ui.OPIPartialRefundActivity
import de.tillhub.paymentengine.opi.ui.OPIPaymentActivity
import de.tillhub.paymentengine.opi.ui.OPIPaymentReversalActivity
import de.tillhub.paymentengine.opi.ui.OPIReconciliationActivity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Objects

/**
 * Represents an OPI (Open Payment Initiative) terminal.
 *
 * @property id The unique identifier of the terminal.
 * @property saleConfig Configuration for card sales.
 * @property ipAddress The IP address of the terminal.
 * @property port The port number for communication on channel 0.
 * @property port2 The port number for communication on channel 1.
 * @property currencyCode The currency code used for transactions (e.g., "EUR").
 * @property contract The contract defining the terminal's capabilities.
 */
@Parcelize
class OpiTerminal private constructor(
    override val id: String = DEFAULT_OPI_ID,
    override val saleConfig: CardSaleConfig = CardSaleConfig(),
    val ipAddress: String = DEFAULT_IP_ADDRESS,
    val port: Int = DEFAULT_PORT_1,
    val port2: Int = DEFAULT_PORT_2,
    val currencyCode: String = DEFAULT_CURRENCY_CODE,
) : Terminal {
    @IgnoredOnParcel
    override val contract: TerminalContract = OpiTerminalContract

    override fun toString() = "OPITerminal(" +
            "id=$id, " +
            "ipAddress=$ipAddress, " +
            "port=$port, " +
            "saleConfig=$saleConfig, " +
            "port2=$port2, " +
            "currencyCode=$currencyCode" +
            ")"

    override fun equals(other: Any?) = other is OpiTerminal &&
            id == other.id &&
            ipAddress == other.ipAddress &&
            port == other.port &&
            saleConfig == other.saleConfig &&
            port2 == other.port2 &&
            currencyCode == other.currencyCode

    override fun hashCode() = Objects.hash(
        id,
        ipAddress,
        port,
        saleConfig,
        port2,
        currencyCode
    )

    companion object {
        private const val DEFAULT_OPI_ID = "Default:OPI"
        const val DEFAULT_IP_ADDRESS = "127.0.0.1"
        const val DEFAULT_PORT_1 = 20002
        const val DEFAULT_PORT_2 = 20007
        const val DEFAULT_CURRENCY_CODE = "EUR"
        const val TYPE = "OPI"

        @Suppress("LongParameterList")
        fun create(
            id: String = DEFAULT_OPI_ID,
            saleConfig: CardSaleConfig = CardSaleConfig(),
            ipAddress: String = DEFAULT_IP_ADDRESS,
            port: Int = DEFAULT_PORT_1,
            port2: Int = DEFAULT_PORT_2,
            currencyCode: String = DEFAULT_CURRENCY_CODE,
        ): OpiTerminal = OpiTerminal(
            id = id,
            saleConfig = saleConfig,
            ipAddress = ipAddress,
            port = port,
            port2 = port2,
            currencyCode = currencyCode
        )
    }
}

/**
 * Implementation of [TerminalContract] for OPI terminals.
 * This object defines how to create Intents for various terminal operations
 * specific to OPI terminals.
 */
internal object OpiTerminalContract : TerminalContract {
    override fun connectIntent(context: Context, terminal: Terminal): Intent =
        Intent(context, OPILoginActivity::class.java).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, terminal)
        }

    override fun paymentIntent(context: Context, input: PaymentRequest): Intent =
        Intent(context, OPIPaymentActivity::class.java).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount + input.tip)
            putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
        }

    override fun refundIntent(context: Context, input: RefundRequest): Intent =
        Intent(context, OPIPartialRefundActivity::class.java).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
            putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
        }

    override fun reversalIntent(context: Context, input: ReversalRequest): Intent =
        Intent(context, OPIPaymentReversalActivity::class.java).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_RECEIPT_NO, input.receiptNo)
        }

    override fun reconciliationIntent(context: Context, terminal: Terminal): Intent =
        Intent(context, OPIReconciliationActivity::class.java).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, terminal)
        }

    override fun recoveryIntent(context: Context, terminal: Terminal): Intent {
        throw UnsupportedOperationException("Payment recovery is not supported by this terminal")
    }

    override fun disconnectIntent(context: Context, terminal: Terminal): Intent {
        throw UnsupportedOperationException("Disconnect is not supported by this terminal")
    }

    override fun ticketReprintIntent(context: Context, terminal: Terminal): Intent {
        throw UnsupportedOperationException("Ticket reprint is not supported by this terminal")
    }
}