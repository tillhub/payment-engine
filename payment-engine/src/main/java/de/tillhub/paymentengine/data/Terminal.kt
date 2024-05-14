package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Terminal : Parcelable {
    abstract val name: String
    abstract val ipAddress: String
    abstract val port: Int
    abstract val saleConfig: CardSaleConfig

    data class ZVT(
        override val name: String = DEFAULT_NAME,
        override val ipAddress: String = DEFAULT_IP_ADDRESS,
        override val port: Int = DEFAULT_PORT,
        override val saleConfig: CardSaleConfig = CardSaleConfig(),
        val terminalPrinterAvailable: Boolean = DEFAULT_PRINTER_AVAILABLE,
        val isoCurrencyNumber: String = DEFAULT_CURRENCY_CODE,
    ) : Terminal() {
        companion object {
            private const val DEFAULT_NAME = "Default:ZVT"
            private const val DEFAULT_IP_ADDRESS = "127.0.0.1"
            private const val DEFAULT_PORT = 40007
            private const val DEFAULT_CURRENCY_CODE = "0978"
            private const val DEFAULT_PRINTER_AVAILABLE = true
        }
    }

    data class OPI(
        override val name: String = DEFAULT_NAME,
        override val ipAddress: String = DEFAULT_IP_ADDRESS,
        override val port: Int = DEFAULT_PORT_1,
        override val saleConfig: CardSaleConfig = CardSaleConfig(),
        val port2: Int = DEFAULT_PORT_2,
        val currencyCode: String = DEFAULT_CURRENCY_CODE,
    ) : Terminal() {
        companion object {
            private const val DEFAULT_NAME = "Default:OPI"
            private const val DEFAULT_IP_ADDRESS = "127.0.0.1"
            private const val DEFAULT_PORT_1 = 20002
            private const val DEFAULT_PORT_2 = 20007
            private const val DEFAULT_CURRENCY_CODE = "EUR"
        }
    }
}