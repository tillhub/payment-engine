package de.tillhub.paymentengine.data

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import de.tillhub.paymentengine.contract.PaymentRequest
import de.tillhub.paymentengine.contract.RefundRequest
import de.tillhub.paymentengine.contract.ReversalRequest
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
sealed class Terminal : Parcelable {
    abstract val id: String

    abstract val saleConfig: CardSaleConfig

    class ZVT(
        override val id: String = DEFAULT_ZVT_ID,
        override val saleConfig: CardSaleConfig = CardSaleConfig(),
        val ipAddress: String = DEFAULT_IP_ADDRESS,
        val port: Int = DEFAULT_PORT,
        val terminalPrinterAvailable: Boolean = DEFAULT_PRINTER_AVAILABLE,
        val isoCurrencyNumber: String = DEFAULT_CURRENCY_CODE,
    ) : Terminal() {
        override fun toString() = "Terminal.ZVT(" +
                "id=$id, " +
                "ipAddress=$ipAddress, " +
                "port=$port, " +
                "saleConfig=$saleConfig, " +
                "terminalPrinterAvailable=$terminalPrinterAvailable, " +
                "isoCurrencyNumber=$isoCurrencyNumber" +
                ")"

        override fun equals(other: Any?) = other is ZVT &&
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
        }
    }

    class OPI(
        override val id: String = DEFAULT_OPI_ID,
        override val saleConfig: CardSaleConfig = CardSaleConfig(),
        val ipAddress: String = DEFAULT_IP_ADDRESS,
        val port: Int = DEFAULT_PORT_1,
        val port2: Int = DEFAULT_PORT_2,
        val currencyCode: String = DEFAULT_CURRENCY_CODE,
    ) : Terminal() {
        override fun toString() = "Terminal.OPI(" +
                "id=$id, " +
                "ipAddress=$ipAddress, " +
                "port=$port, " +
                "saleConfig=$saleConfig, " +
                "port2=$port2, " +
                "currencyCode=$currencyCode" +
                ")"

        override fun equals(other: Any?) = other is OPI &&
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
        }
    }

    open class External(
        override val id: String = DEFAULT_EXTERNAL_ID,
        override val saleConfig: CardSaleConfig = CardSaleConfig(),
    ) : Terminal() {
        open fun connectIntent(context: Context, input: Terminal): Intent {
            throw UnsupportedOperationException("Connect is not supported by this terminal")
        }
        open fun paymentIntent(context: Context, input: PaymentRequest): Intent {
            throw UnsupportedOperationException("Payment is not supported by this terminal")
        }
        open fun refundIntent(context: Context, input: RefundRequest): Intent {
            throw UnsupportedOperationException("Refund is not supported by this terminal")
        }
        open fun reversalIntent(context: Context, input: ReversalRequest): Intent {
            throw UnsupportedOperationException(
                "Payment reversal is not supported by this terminal"
            )
        }
        open fun reconciliationIntent(context: Context, input: Terminal): Intent {
            throw UnsupportedOperationException("Reconciliation is not supported by this terminal")
        }
        open fun ticketReprintIntent(context: Context, input: Terminal): Intent {
            throw UnsupportedOperationException("Ticket reprint is not supported by this terminal")
        }
        open fun recoveryIntent(context: Context, input: Terminal): Intent {
            throw UnsupportedOperationException("Payment recovery is not supported by this terminal")
        }
        open fun disconnectIntent(context: Context, input: Terminal): Intent {
            throw UnsupportedOperationException("Disconnect is not supported by this terminal")
        }

        override fun toString() = "Terminal.External(" +
                "id=$id, " +
                "saleConfig=$saleConfig" +
                ")"

        override fun equals(other: Any?) = other is External &&
                id == other.id &&
                saleConfig == other.saleConfig

        override fun hashCode() = Objects.hash(
            id,
            saleConfig,
        )

        companion object {
            private const val DEFAULT_EXTERNAL_ID = "Default:External"
        }
    }
}