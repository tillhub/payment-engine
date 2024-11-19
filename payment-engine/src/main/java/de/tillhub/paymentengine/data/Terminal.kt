package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
sealed class Terminal : Parcelable {
    abstract val name: String

    abstract val saleConfig: CardSaleConfig

    class ZVT(
        override val name: String = DEFAULT_NAME,
        override val saleConfig: CardSaleConfig = CardSaleConfig(),
        val ipAddress: String = DEFAULT_IP_ADDRESS,
        val port: Int = DEFAULT_PORT,
        val terminalPrinterAvailable: Boolean = DEFAULT_PRINTER_AVAILABLE,
        val isoCurrencyNumber: String = DEFAULT_CURRENCY_CODE,
    ) : Terminal() {
        override fun toString() = "Terminal.ZVT(" +
                "name=$name, " +
                "ipAddress=$ipAddress, " +
                "port=$port, " +
                "saleConfig=$saleConfig, " +
                "terminalPrinterAvailable=$terminalPrinterAvailable, " +
                "isoCurrencyNumber=$isoCurrencyNumber" +
                ")"

        override fun equals(other: Any?) = other is ZVT &&
                name == other.name &&
                ipAddress == other.ipAddress &&
                port == other.port &&
                saleConfig == other.saleConfig &&
                terminalPrinterAvailable == other.terminalPrinterAvailable &&
                isoCurrencyNumber == other.isoCurrencyNumber

        override fun hashCode() = Objects.hash(
            name,
            ipAddress,
            port,
            saleConfig,
            terminalPrinterAvailable,
            isoCurrencyNumber
        )

        companion object {
            private const val DEFAULT_NAME = "Default:ZVT"
            const val DEFAULT_IP_ADDRESS = "127.0.0.1"
            const val DEFAULT_PORT = 40007
            const val DEFAULT_CURRENCY_CODE = "0978"
            private const val DEFAULT_PRINTER_AVAILABLE = true
        }
    }

    class OPI(
        override val name: String = DEFAULT_NAME,
        override val saleConfig: CardSaleConfig = CardSaleConfig(),
        val ipAddress: String = DEFAULT_IP_ADDRESS,
        val port: Int = DEFAULT_PORT_1,
        val port2: Int = DEFAULT_PORT_2,
        val currencyCode: String = DEFAULT_CURRENCY_CODE,
    ) : Terminal() {
        override fun toString() = "Terminal.OPI(" +
                "name=$name, " +
                "ipAddress=$ipAddress, " +
                "port=$port, " +
                "saleConfig=$saleConfig, " +
                "port2=$port2, " +
                "currencyCode=$currencyCode" +
                ")"

        override fun equals(other: Any?) = other is OPI &&
                name == other.name &&
                ipAddress == other.ipAddress &&
                port == other.port &&
                saleConfig == other.saleConfig &&
                port2 == other.port2 &&
                currencyCode == other.currencyCode

        override fun hashCode() = Objects.hash(
            name,
            ipAddress,
            port,
            saleConfig,
            port2,
            currencyCode
        )

        companion object {
            private const val DEFAULT_NAME = "Default:OPI"
            const val DEFAULT_IP_ADDRESS = "127.0.0.1"
            const val DEFAULT_PORT_1 = 20002
            const val DEFAULT_PORT_2 = 20007
            const val DEFAULT_CURRENCY_CODE = "EUR"
        }
    }

    class SPOS(
        override val name: String = DEFAULT_NAME,
        override val saleConfig: CardSaleConfig = CardSaleConfig(),
        val appId: String = DEFAULT_APP_ID,
        val connected: Boolean = DEFAULT_CONNECTION,
        val currencyCode: String = DEFAULT_CURRENCY_CODE,
    ) : Terminal() {
        override fun toString() = "Terminal.SPOS(" +
                "name=$name, " +
                "appId=$appId, " +
                "saleConfig=$saleConfig, " +
                "currencyCode=$currencyCode" +
                ")"

        override fun equals(other: Any?) = other is SPOS &&
                name == other.name &&
                appId == other.appId &&
                saleConfig == other.saleConfig &&
                currencyCode == other.currencyCode

        override fun hashCode() = Objects.hash(
            name,
            appId,
            saleConfig,
            currencyCode
        )

        companion object {
            private const val DEFAULT_NAME = "Default:SPOS"
            private const val DEFAULT_APP_ID = "TESTCLIENT"
            private const val DEFAULT_CONNECTION = false
            const val DEFAULT_CURRENCY_CODE = "EUR"
        }
    }
}