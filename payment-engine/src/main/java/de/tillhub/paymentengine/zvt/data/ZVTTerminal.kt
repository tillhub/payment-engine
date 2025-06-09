package de.tillhub.paymentengine.zvt.data

import android.content.Context
import android.content.Intent
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.ExtraKeys
import de.tillhub.paymentengine.data.Terminal
import de.tillhub.paymentengine.data.TerminalContract
import de.tillhub.paymentengine.zvt.ui.CardPaymentActivity
import de.tillhub.paymentengine.zvt.ui.CardPaymentPartialRefundActivity
import de.tillhub.paymentengine.zvt.ui.CardPaymentReversalActivity
import de.tillhub.paymentengine.zvt.ui.TerminalLoginActivity
import de.tillhub.paymentengine.zvt.ui.TerminalReconciliationActivity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Suppress("LongParameterList")
@Parcelize
class ZVTTerminal internal constructor(
    override val id: String = DEFAULT_ZVT_ID,
    override val saleConfig: CardSaleConfig = CardSaleConfig(),
    @IgnoredOnParcel
    override val contract: TerminalContract = ZVTTerminalContract(),
    val ipAddress: String = DEFAULT_IP_ADDRESS,
    val port: Int = DEFAULT_PORT,
    val terminalPrinterAvailable: Boolean = DEFAULT_PRINTER_AVAILABLE,
    val isoCurrencyNumber: String = DEFAULT_CURRENCY_CODE,
) : Terminal {
    override fun toString() = "ZVTTerminal(" +
            "id=$id, " +
            "ipAddress=$ipAddress, " +
            "port=$port, " +
            "saleConfig=$saleConfig, " +
            "terminalPrinterAvailable=$terminalPrinterAvailable, " +
            "isoCurrencyNumber=$isoCurrencyNumber" +
            ")"

    override fun equals(other: Any?) = other is ZVTTerminal &&
            id == other.id &&
            ipAddress == other.ipAddress &&
            port == other.port &&
            saleConfig == other.saleConfig &&
            terminalPrinterAvailable == other.terminalPrinterAvailable &&
            isoCurrencyNumber == other.isoCurrencyNumber

    override fun hashCode() = Objects.hash(
        id,
        ipAddress,
        port,
        saleConfig,
        terminalPrinterAvailable,
        isoCurrencyNumber
    )

    companion object {
        private const val DEFAULT_ZVT_ID = "Default:ZVT"
        const val DEFAULT_IP_ADDRESS = "127.0.0.1"
        const val DEFAULT_PORT = 40007
        const val DEFAULT_CURRENCY_CODE = "0978"
        private const val DEFAULT_PRINTER_AVAILABLE = true
        const val TYPE = "ZVT"

        fun create(
            id: String = DEFAULT_ZVT_ID,
            saleConfig: CardSaleConfig = CardSaleConfig(),
            ipAddress: String = DEFAULT_IP_ADDRESS,
            port: Int = DEFAULT_PORT,
            terminalPrinterAvailable: Boolean = DEFAULT_PRINTER_AVAILABLE,
            isoCurrencyNumber: String = DEFAULT_CURRENCY_CODE
        ): ZVTTerminal = ZVTTerminal(
            id = id,
            saleConfig = saleConfig,
            ipAddress = ipAddress,
            port = port,
            terminalPrinterAvailable = terminalPrinterAvailable,
            isoCurrencyNumber = isoCurrencyNumber
        )
    }
}

internal class ZVTTerminalContract : TerminalContract {
    override fun connectIntent(context: Context, terminal: Terminal): Intent =
        Intent(context, TerminalLoginActivity::class.java).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, terminal)
        }

    override fun paymentIntent(context: Context, input: PaymentRequest): Intent =
        Intent(context, CardPaymentActivity::class.java).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount + input.tip)
            putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
        }

    override fun refundIntent(context: Context, input: RefundRequest): Intent =
        Intent(context, CardPaymentPartialRefundActivity::class.java).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_AMOUNT, input.amount)
            putExtra(ExtraKeys.EXTRA_CURRENCY, input.currency)
        }

    override fun reversalIntent(context: Context, input: ReversalRequest): Intent =
        Intent(context, CardPaymentReversalActivity::class.java).apply {
            putExtra(ExtraKeys.EXTRA_CONFIG, input.config)
            putExtra(ExtraKeys.EXTRA_RECEIPT_NO, input.receiptNo)
        }

    override fun reconciliationIntent(context: Context, terminal: Terminal): Intent =
        Intent(context, TerminalReconciliationActivity::class.java).apply {
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