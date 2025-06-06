package de.tillhub.paymentengine.zvt.data

import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.Terminal
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
class ZVTTerminal(
    override val id: String = DEFAULT_ZVT_ID,
    override val saleConfig: CardSaleConfig = CardSaleConfig(),
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
    }
}