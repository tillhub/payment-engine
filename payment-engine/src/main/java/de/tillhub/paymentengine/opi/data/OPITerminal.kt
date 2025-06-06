package de.tillhub.paymentengine.opi.data

import de.tillhub.paymentengine.data.CardSaleConfig
import de.tillhub.paymentengine.data.Terminal
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
class OPITerminal(
    override val id: String = DEFAULT_OPI_ID,
    override val saleConfig: CardSaleConfig = CardSaleConfig(),
    val ipAddress: String = DEFAULT_IP_ADDRESS,
    val port: Int = DEFAULT_PORT_1,
    val port2: Int = DEFAULT_PORT_2,
    val currencyCode: String = DEFAULT_CURRENCY_CODE,
) : Terminal {
    override fun toString() = "OPITerminal(" +
            "id=$id, " +
            "ipAddress=$ipAddress, " +
            "port=$port, " +
            "saleConfig=$saleConfig, " +
            "port2=$port2, " +
            "currencyCode=$currencyCode" +
            ")"

    override fun equals(other: Any?) = other is OPITerminal &&
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