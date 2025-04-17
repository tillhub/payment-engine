package de.tillhub.paymentengine.data

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
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

    class SPOS(
        override val id: String = DEFAULT_SPOS_ID,
        override val saleConfig: CardSaleConfig = CardSaleConfig(),
        val appId: String = DEFAULT_APP_ID,
        val connected: Boolean = DEFAULT_CONNECTION,
        val currencyCode: String = DEFAULT_CURRENCY_CODE,
    ) : Terminal() {
        override fun toString() = "Terminal.SPOS(" +
                "id=$id, " +
                "appId=$appId, " +
                "saleConfig=$saleConfig, " +
                "currencyCode=$currencyCode" +
                ")"

        override fun equals(other: Any?) = other is SPOS &&
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

    open class External(
        override val id: String = DEFAULT_EXTERNAL_ID,
        override val saleConfig: CardSaleConfig = CardSaleConfig(),
    ) : Terminal() {
        @IgnoredOnParcel
        open val connectActivity: Class<*>? = null
        @IgnoredOnParcel
        open val paymentActivity: Class<*>? = null
        @IgnoredOnParcel
        open val refundActivity: Class<*>? = null
        @IgnoredOnParcel
        open val reversalActivity: Class<*>? = null
        @IgnoredOnParcel
        open val reconciliationActivity: Class<*>? = null

        override fun toString() = "Terminal.External(" +
                "id=$id, " +
                "saleConfig=$saleConfig" +
                ")"

        override fun equals(other: Any?) = other is SPOS &&
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